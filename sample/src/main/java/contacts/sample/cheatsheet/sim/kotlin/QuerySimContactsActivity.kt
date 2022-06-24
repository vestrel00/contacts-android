package contacts.sample.cheatsheet.sim.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.SimContact

class QuerySimContactsActivity : Activity() {

    fun getAllSimContacts(): List<SimContact> = Contacts(this).sim().query().find()

    fun getAllSimContactsWithPhoneNumber(): List<SimContact> = Contacts(this)
        .sim()
        .query()
        .find()
        .filter { !it.number.isNullOrEmpty() }
}