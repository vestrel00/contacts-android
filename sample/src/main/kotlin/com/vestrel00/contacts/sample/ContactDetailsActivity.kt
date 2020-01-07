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
        }
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