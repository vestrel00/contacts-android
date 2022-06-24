package contacts.sample.cheatsheet.blockednumbers.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.entities.BlockedNumber

class QueryBlockedNumbersActivity : Activity() {

    fun getAllBlockedNumbers(): List<BlockedNumber> =
        Contacts(this).blockedNumbers().query().find()

    fun getBlockedNumbersContainingNumber(number: String): List<BlockedNumber> = Contacts(this)
        .blockedNumbers()
        .query()
        .where { (Number contains number) or (NormalizedNumber contains number) }
        .find()
}