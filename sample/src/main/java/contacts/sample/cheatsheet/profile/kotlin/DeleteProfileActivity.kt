package contacts.sample.cheatsheet.profile.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.profile.ProfileDelete

class DeleteProfileActivity : Activity() {

    fun deleteProfile(): ProfileDelete.Result = Contacts(this).profile().delete().contact().commit()
}