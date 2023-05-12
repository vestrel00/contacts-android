package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.aggregationexceptions.ContactLink
import contacts.core.aggregationexceptions.ContactUnlink
import contacts.core.entities.Contact
import contacts.core.util.linkDirect
import contacts.core.util.unlinkDirect

class ContactLinksActivity : Activity() {

    fun link(contacts: List<Contact>): ContactLink.Result = Contacts(this)
        .aggregationExceptions()
        .link()
        .contacts(contacts)
        .commit()

    fun unlink(contact: Contact): ContactUnlink.Result = Contacts(this)
        .aggregationExceptions()
        .unlink()
        .contact(contact)
        .commit()

    fun linkDirect(contacts: List<Contact>): ContactLink.Result =
        contacts.linkDirect(Contacts(this))

    fun unlinkDirect(contact: Contact): ContactUnlink.Result =
        contact.unlinkDirect(Contacts(this))
}