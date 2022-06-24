package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.*
import contacts.core.util.*

class ContactAndRawContactOptionsActivity : Activity() {

    fun getContactOptions(contact: Contact): Options? = contact.options

    fun getContactOptionsFromDb(contact: Contact): Options? = contact.options(Contacts(this))

    fun getRawContactOptionsFromDb(rawContact: RawContact): Options? =
        rawContact.options(Contacts(this))

    fun setContactOptions(contact: Contact, options: MutableOptions): Boolean =
        contact.setOptions(Contacts(this), options)

    fun setRawContactOptions(rawContact: RawContact, options: MutableOptions): Boolean =
        rawContact.setOptions(Contacts(this), options)
}