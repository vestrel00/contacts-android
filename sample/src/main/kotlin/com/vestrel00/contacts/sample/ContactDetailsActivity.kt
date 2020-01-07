package com.vestrel00.contacts.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.vestrel00.contacts.Contacts
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.async.findFirstAsync
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.equalTo
import com.vestrel00.contacts.permissions.queryWithPermission
import com.vestrel00.contacts.util.names
import kotlinx.android.synthetic.main.activity_contact_details.*
import kotlinx.coroutines.launch

class ContactDetailsActivity : BaseActivity() {

    private lateinit var contact: MutableContact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)

        val activity: Activity = this@ContactDetailsActivity
        launch {
            if (!fetchContact()) {
                Toast.makeText(activity, R.string.contact_details_fetch_error, LENGTH_SHORT).show()
                finish()
                return@launch
            }

            // TODO Add linked contacts field
            setupPhotoView()
            setupNameFields()
            setupPhoneFields()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoView.onActivityResult(requestCode, resultCode, data)
    }

    private suspend fun fetchContact(): Boolean {
        val result = Contacts().queryWithPermission(this)
            .where(Fields.Contact.Id equalTo intent.contactId())
            .findFirstAsync()

        if (result != null) {
            contact = result.toMutableContact()
            return true
        }

        return false
    }

    private fun setupPhotoView() {
        photoView.init(this)
        photoView.contact = contact
    }

    private fun setupNameFields() {
        // TODO Move this to a custom view in contacts-ui and handle multiple names the same way the
        // native Contacts app does. For now just pick the first name, if any.
        val name = contact.names().firstOrNull()
        namePrefixField.setText(name?.prefix)
        firstNameField.setText(name?.givenName)
        middleNameField.setText(name?.middleName)
        lastNameField.setText(name?.familyName)
        nameSuffixField.setText(name?.suffix)
    }

    private fun setupPhoneFields() {
        phonesView.contact = contact
    }

    companion object {

        fun showContactDetails(activity: Activity, contactId: Long) {
            val intent = Intent(activity, ContactDetailsActivity::class.java).apply {
                putExtra(CONTACT_ID, contactId)
            }

            activity.startActivityForResult(intent, REQUEST_CONTACT_DETAILS)
        }

        fun onShowContactDetailsResult(requestCode: Int, contactDetailsShown: () -> Unit) {
            if (requestCode == REQUEST_CONTACT_DETAILS) {
                contactDetailsShown()
            }
        }

        private fun Intent.contactId(): Long = getLongExtra(CONTACT_ID, -1L)

        private const val REQUEST_CONTACT_DETAILS = 111
        private const val CONTACT_ID = "contactId"
    }
}