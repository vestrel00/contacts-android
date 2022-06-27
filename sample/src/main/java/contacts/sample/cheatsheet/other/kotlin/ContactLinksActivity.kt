package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.util.*

class ContactLinksActivity : Activity() {

    fun linkContacts(contacts: List<Contact>): ContactLinkResult = contacts.link(Contacts(this))

    fun unlinkContact(contact: Contact): ContactUnlinkResult = contact.unlink(Contacts(this))
}