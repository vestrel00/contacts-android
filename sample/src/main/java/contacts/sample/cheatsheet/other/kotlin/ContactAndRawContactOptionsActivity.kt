package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.*
import contacts.core.util.*

class ContactAndRawContactOptionsActivity : Activity() {

    fun getContactOptions(contact: Contact): Options? = contact.options

    fun setContactOptions(contact: Contact) {
        Contacts(this)
            .update()
            .contacts(
                contact.mutableCopy {
                    setOptions {
                        starred = true
                        customRingtone = null
                        sendToVoicemail = false

                    }
                }
            )
            .commit()
    }

    fun getRawContactOptions(rawContact: RawContact): Options? = rawContact.options

    fun setRawContactOptions(rawContact: RawContact) {
        Contacts(this)
            .update()
            .rawContacts(
                rawContact.mutableCopy {
                    setOptions {
                        starred = true
                        customRingtone = null
                        sendToVoicemail = false

                    }
                }
            )
            .commit()
    }

    fun insertNewRawContactWithOptions() {
        Contacts(this)
            .insert()
            .rawContact {
                setOptions { starred = true }
                setNickname { name = "Favorite friend" }
            }
            .commit()
    }
}