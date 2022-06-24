package contacts.sample.cheatsheet.sim.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.SimContact
import contacts.core.sim.SimContactsUpdate

class UpdateSimContactsActivity : Activity() {

    fun updateSimContact(simContact: SimContact): SimContactsUpdate.Result = Contacts(this)
        .sim()
        .update()
        .simContact(simContact, simContact.mutableCopy {
            name = "Vandolf"
            number = "1234567890"
        })
        .commit()
}