package contacts.sample.cheatsheet.blockednumbers.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.blockednumbers.BlockedNumbersInsert

class InsertBlockedNumbersActivity : Activity() {

    fun insertBlockedNumber(): BlockedNumbersInsert.Result = Contacts(this)
        .blockedNumbers()
        .insert()
        .blockedNumber {
            number = "555-555-5555"
        }
        .commit()
}