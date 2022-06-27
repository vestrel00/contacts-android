package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.log.AndroidLogger

class LogContactsApiActivity : Activity() {

    fun createContactsApiWithLoggingEnabled(redactLogMessages: Boolean): Contacts = Contacts(
        this, logger = AndroidLogger(redactMessages = redactLogMessages)
    )
}