package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.aggregationexceptions.ContactLink
import contacts.core.aggregationexceptions.ContactUnlink
import contacts.core.entities.Contact
import contacts.core.util.linkDirect
import contacts.core.util.unlinkDirect

class ContactLinksActivity : Activity() {

    fun linkContacts(contacts: List<Contact>): ContactLink.Result = Contacts(this)
        .aggregationExceptions()
        .linkContacts()
        .contacts(contacts)
        .commit()

    fun unlinkContact(contact: Contact): ContactUnlink.Result = Contacts(this)
        .aggregationExceptions()
        .unlinkContact()
        .contact(contact)
        .commit()

    fun linkContactsDirect(contacts: List<Contact>): ContactLink.Result =
        contacts.linkDirect(Contacts(this))

    fun unlinkContactDirect(contact: Contact): ContactUnlink.Result =
        contact.unlinkDirect(Contacts(this))
}