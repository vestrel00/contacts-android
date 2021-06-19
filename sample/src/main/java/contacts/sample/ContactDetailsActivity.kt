package contacts.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import contacts.Contacts
import contacts.Fields
import contacts.async.commitWithContext
import contacts.async.findWithContext
import contacts.entities.MutableContact
import contacts.equalTo
import contacts.permissions.queryWithPermission
import contacts.permissions.updateWithPermission
import contacts.sample.ContactDetailsActivity.Companion.CONTACT_ID
import contacts.sample.ContactDetailsActivity.Companion.Mode
import contacts.sample.databinding.ActivityContactDetailsBinding
import contacts.util.names
import kotlinx.coroutines.launch

/**
 * Shows all Data of all RawContacts associated with the given Contact with [CONTACT_ID].
 *
 * #### Modes
 *
 * - [Mode.VIEW]: Displays all existing Contact/RawContact(s) Data. Modifications are NOT allowed.
 * - [Mode.EDIT]: Displays all existing Contact/RawContact(s) Data. Modifications are allowed.
 * - [Mode.CREATE]: Displays an empty Contact/RawContact form for creating a new Contact/RawContact.
 */
class ContactDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityContactDetailsBinding

    private lateinit var contact: MutableContact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        launch {
            initializeMode()
        }
    }

    private suspend fun initializeMode() {
        when (intent.mode) {
            Mode.VIEW -> initializeViewMode()
            Mode.EDIT -> initializeEditMode()
            Mode.CREATE -> initializeCreateMode()
        }
    }

    private suspend fun initializeViewMode() {
        initializeEditMode()
        // TODO disable all views.
    }

    private suspend fun initializeEditMode() {
        if (!fetchContact()) {
            Toast.makeText(this, R.string.edit_contact_details_fetch_error, LENGTH_SHORT)
                .show()
            finish()
            return
        }

        // TODO Add linked contacts field
        setupPhotoView()
        setupNameFields()
        setupPhoneFields()
    }

    private suspend fun initializeCreateMode() {
        // TODO
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_contact_details, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.save -> {
                launch { save() }
                return true
            }
        }

        return super.onOptionsItemSelected(menuItem)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.photoView.onActivityResult(requestCode, resultCode, data)
    }

    private suspend fun fetchContact(): Boolean {
        val result = Contacts(this).queryWithPermission()
            .where(Fields.Contact.Id equalTo intent.contactId)
            .findWithContext()
            .firstOrNull()

        if (result != null) {
            contact = result.toMutableContact()
            return true
        }

        return false
    }

    private fun setupPhotoView() {
        binding.photoView.init(this)
        binding.photoView.contact = contact
    }

    private fun setupNameFields() {
        // TODO Move this to a custom view in contacts-ui and handle multiple names the same way the
        // native Contacts app does. For now just pick the first name, if any.
        val name = contact.names().firstOrNull()
        binding.namePrefixField.setText(name?.prefix)
        binding.firstNameField.setText(name?.givenName)
        binding.middleNameField.setText(name?.middleName)
        binding.lastNameField.setText(name?.familyName)
        binding.nameSuffixField.setText(name?.suffix)
    }

    private fun setupPhoneFields() {
        binding.phonesView.contact = contact
    }

    private suspend fun save(): Boolean {
        showProgressDialog()

        // Save photo first so that the Contact does not get deleted if it only has a photo.
        // Blank Contacts are by default deleted in updates.
        val photoSaveSuccess = binding.photoView.saveContactPhoto()

        // Save changes. Delete blanks!
        val contactSaveResult = Contacts(this).updateWithPermission()
            // This is implicitly true by default. We are just being explicitly verbose here.
            .deleteBlanks(true)
            .contacts(contact)
            .commitWithContext()

        val success = contactSaveResult.isSuccessful && photoSaveSuccess

        val resultMessageRes = if (success) {
            R.string.edit_contact_details_save_success
        } else {
            R.string.edit_contact_details_save_error
        }
        Toast.makeText(this, resultMessageRes, LENGTH_SHORT).show()

        dismissProgressDialog()

        return success
    }

    companion object {

        // region VIEW
        fun viewContactDetails(activity: Activity, contactId: Long) {
            val intent = Intent(activity, ContactDetailsActivity::class.java).apply {
                putExtra(REQUEST_CODE, REQUEST_VIEW)
                putExtra(CONTACT_ID, contactId)
            }

            activity.startActivityForResult(intent, REQUEST_VIEW)
        }

        fun onViewContactDetailsResult(requestCode: Int, contactDetailsViewed: () -> Unit) {
            if (requestCode == REQUEST_VIEW) {
                contactDetailsViewed()
            }
        }
        // endregion

        // region EDIT
        fun editContactDetails(activity: Activity, contactId: Long) {
            val intent = Intent(activity, ContactDetailsActivity::class.java).apply {
                putExtra(REQUEST_CODE, REQUEST_EDIT)
                putExtra(CONTACT_ID, contactId)
            }

            activity.startActivityForResult(intent, REQUEST_EDIT)
        }

        fun onEditContactDetailsResult(requestCode: Int, contactDetailsEdited: () -> Unit) {
            if (requestCode == REQUEST_EDIT) {
                contactDetailsEdited()
            }
        }
        // endregion

        // region CREATE
        fun createContact(activity: Activity) {
            val intent = Intent(activity, ContactDetailsActivity::class.java).apply {
                putExtra(REQUEST_CODE, REQUEST_CREATE)
            }

            intent.type

            activity.startActivityForResult(intent, REQUEST_CREATE)
        }

        fun onCreateContactResult(requestCode: Int, contactDetailsViewed: () -> Unit) {
            if (requestCode == REQUEST_CREATE) {
                contactDetailsViewed()
            }
        }
        // endregion

        private enum class Mode {
            VIEW, EDIT, CREATE
        }

        private val Intent.mode: Mode
            get() = when (requestCode) {
                REQUEST_VIEW -> Mode.VIEW
                REQUEST_EDIT -> Mode.EDIT
                REQUEST_CREATE -> Mode.CREATE
                else -> {
                    throw IllegalArgumentException("There is no mode for request code $requestCode")
                }
            }

        /**
         * The type of request. Defaults to -1 if not provided.
         *
         * #### Dev note
         *
         * I'm aware that [Intent.getAction] paired with [Intent.getData] are more appropriate tools
         * to use than the request code. Using these tools with the correct scheme will enable deep
         * linking into this sample app. For example, launching an intent from any app with matching
         * action and scheme will result in the suggestion of the native Contacts app or this sample
         * app to handle the intent. See [android.provider.ContactsContract.Intents].
         *
         * TODO? Implement deep linking?
         * Anyways, this is not something I'll be implementing in this sample app, at least
         * initially, as it requires a bit of work to get right. This is something actual apps may
         * implement but this sample app may implement it too so it can be used as reference. We may
         * even create a library containing deep linking functions?
         */
        private val Intent.requestCode: Int
            get() = getIntExtra(REQUEST_CODE, -1)

        /**
         * The id of an existing Contact. Defaults to -1 if not provided.
         */
        private val Intent.contactId: Long
            get() = getLongExtra(CONTACT_ID, -1L)

        private const val REQUEST_VIEW = 111
        private const val REQUEST_EDIT = 222
        private const val REQUEST_CREATE = 333

        private const val REQUEST_CODE = "requestCode"
        private const val CONTACT_ID = "contactId"
    }
}