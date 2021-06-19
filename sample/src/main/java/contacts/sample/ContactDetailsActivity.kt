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
import contacts.async.findWithContext
import contacts.entities.MutableContact
import contacts.equalTo
import contacts.permissions.queryWithPermission
import contacts.sample.ContactDetailsActivity.Companion.CONTACT_ID
import contacts.sample.ContactDetailsActivity.Companion.Mode
import contacts.sample.view.ContactView
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

    // Not using any view binding libraries or plugins just for this.
    private lateinit var contactView: ContactView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)
        contactView = findViewById(R.id.contactView)

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
        val contact = fetchContact()
        if (contact == null) {
            Toast.makeText(this, R.string.contact_details_fetch_error, LENGTH_SHORT)
                .show()
            finish()
            return
        }

        contactView.contact = contact
    }

    private fun initializeCreateMode() {
        TODO("initializeCreateMode")
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
        contactView.onActivityResult(requestCode, resultCode, data)
    }

    private suspend fun fetchContact(): MutableContact? {
        val result = Contacts(this).queryWithPermission()
            .where(Fields.Contact.Id equalTo intent.contactId)
            .findWithContext()
            .firstOrNull()

        return result?.toMutableContact()
    }

    private suspend fun save() {
        showProgressDialog()

        val resultMessageRes = if (contactView.saveContact()) {
            R.string.contact_details_save_success
        } else {
            R.string.contact_details_save_error
        }
        Toast.makeText(this, resultMessageRes, LENGTH_SHORT).show()

        dismissProgressDialog()
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