package contacts.sample.cheatsheet.profile.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.Contact

class QueryProfileActivity : Activity() {

    fun getProfile(): Contact? = Contacts(this).profile().query().find().contact
}