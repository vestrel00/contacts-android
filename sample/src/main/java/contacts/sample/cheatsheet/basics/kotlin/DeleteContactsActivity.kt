package contacts.sample.cheatsheet.basics.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.Delete
import contacts.core.entities.Contact
import contacts.core.entities.RawContact

class DeleteContactsActivity : Activity() {

    fun deleteContact(contact: Contact): Delete.Result = Contacts(this)
        .delete()
        .contacts(contact)
        .commit()

    fun deleteContactWithId(contactId: Long): Delete.Result = Contacts(this)
        .delete()
        .contactsWithId(contactId)
        .commit()

    fun deleteRawContact(rawContact: RawContact): Delete.Result = Contacts(this)
        .delete()
        .rawContacts(rawContact)
        .commit()

    fun deleteRawContactWithId(rawContactId: Long): Delete.Result = Contacts(this)
        .delete()
        .rawContactsWithId(rawContactId)
        .commit()
}