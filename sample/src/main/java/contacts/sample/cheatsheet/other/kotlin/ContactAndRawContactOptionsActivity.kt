package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.core.entities.*
import contacts.core.util.setOptions

class ContactAndRawContactOptionsActivity : Activity() {

    fun getContactOptions(contact: Contact): Options? = contact.options

    fun setContactOptions(contact: Contact) {
        contact.mutableCopy {
            setOptions {
                starred = true
                customRingtone = null
                sendToVoicemail = false

            }
        }
    }

    fun getRawContactOptions(rawContact: RawContact): Options? = rawContact.options

    fun setRawContactOptions(rawContact: RawContact) {
        rawContact.mutableCopy {
            setOptions {
                starred = true
                customRingtone = null
                sendToVoicemail = false

            }
        }
    }
}