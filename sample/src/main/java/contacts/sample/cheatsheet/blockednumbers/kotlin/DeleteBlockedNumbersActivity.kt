package contacts.sample.cheatsheet.blockednumbers.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.blockednumbers.BlockedNumbersDelete
import contacts.core.contains
import contacts.core.entities.BlockedNumber

class DeleteBlockedNumbersActivity : Activity() {

    fun deleteBlockedNumber(blockedNumber: BlockedNumber): BlockedNumbersDelete.Result =
        Contacts(this)
            .blockedNumbers()
            .delete()
            .blockedNumbers(blockedNumber)
            .commit()

    fun deleteBlockedNumberWithId(blockedNumberId: Long): BlockedNumbersDelete.Result =
        Contacts(this)
            .blockedNumbers()
            .delete()
            .blockedNumbersWithId(blockedNumberId)
            .commit()

    fun deleteBlockedNumbersContaining555(): BlockedNumbersDelete.Result =
        Contacts(this)
            .blockedNumbers()
            .delete()
            .blockedNumbersWhere { Number contains "555" }
            .commit()
}