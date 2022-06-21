package contacts.sample.cheatsheet.profile.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.*
import contacts.core.profile.ProfileUpdate
import contacts.core.util.*

class UpdateProfileActivity : Activity() {

    fun updateProfile(profile: Contact): ProfileUpdate.Result = Contacts(this)
        .profile()
        .update()
        .contact(profile.mutableCopy {
            setName {
                displayName = "I am the phone owner"
            }
            addEmail {
                type = EmailEntity.Type.CUSTOM
                label = "Profile Email"
                address = "phone@owner.com"
            }
            removeAllPhones()
            setOrganization(null)
        })
        .commit()
}