package contacts.sample.cheatsheet.sim.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.sim.SimContactsInsert

class InsertSimContactsActivity : Activity() {

    fun insertSimContact(): SimContactsInsert.Result = Contacts(this)
        .sim()
        .insert()
        .simContact {
            name = "Mr. Joe"
            number = "5555555555"
        }
        .commit()
}