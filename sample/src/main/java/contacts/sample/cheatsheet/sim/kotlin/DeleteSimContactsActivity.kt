package contacts.sample.cheatsheet.sim.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.SimContact
import contacts.core.sim.SimContactsDelete

class DeleteSimContactsActivity : Activity() {

    fun deleteSimContact(simContact: SimContact): SimContactsDelete.Result =
        Contacts(this).sim().delete().simContacts(simContact).commit()

    fun deleteSimContactWithNameAndNumber(name: String?, number: String?): SimContactsDelete.Result =
        Contacts(this).sim().delete().simContact(name, number).commit()
}