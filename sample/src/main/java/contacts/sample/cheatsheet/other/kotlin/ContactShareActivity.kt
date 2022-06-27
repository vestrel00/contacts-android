package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import android.content.Intent
import contacts.core.entities.Contact
import contacts.core.util.*

class ContactShareActivity : Activity() {

    fun shareContact(contact: Contact) {
        val shareIntent = contact.shareVCardIntent()
        if (shareIntent != null) {
            startActivity(Intent.createChooser(shareIntent, null))
        }
    }

    fun shareContacts(contacts: List<Contact>) {
        val shareIntent = contacts.shareMultiVCardIntent()
        if (shareIntent != null) {
            startActivity(Intent.createChooser(shareIntent, null))
        }
    }
}