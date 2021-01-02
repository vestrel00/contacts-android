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
import contacts.sample.databinding.ActivityEditContactDetailsBinding
import contacts.util.names
import kotlinx.coroutines.launch

class EditContactDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityEditContactDetailsBinding

    private lateinit var contact: MutableContact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditContactDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val activity: Activity = this@EditContactDetailsActivity
        launch {
            if (!fetchContact()) {
                Toast.makeText(activity, R.string.edit_contact_details_fetch_error, LENGTH_SHORT)
                    .show()
                finish()
                return@launch
            }

            // TODO Add linked contacts field
            setupPhotoView()
            setupNameFields()
            setupPhoneFields()
        }
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
            .where(Fields.Contact.Id equalTo intent.contactId())
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

        fun editContactDetails(activity: Activity, contactId: Long) {
            val intent = Intent(activity, EditContactDetailsActivity::class.java).apply {
                putExtra(CONTACT_ID, contactId)
            }

            activity.startActivityForResult(intent, REQUEST_EDIT_CONTACT_DETAILS)
        }

        fun onEditContactDetailsResult(requestCode: Int, contactDetailsEdited: () -> Unit) {
            if (requestCode == REQUEST_EDIT_CONTACT_DETAILS) {
                contactDetailsEdited()
            }
        }

        private fun Intent.contactId(): Long = getLongExtra(CONTACT_ID, -1L)

        private const val REQUEST_EDIT_CONTACT_DETAILS = 111
        private const val CONTACT_ID = "contactId"
    }
}