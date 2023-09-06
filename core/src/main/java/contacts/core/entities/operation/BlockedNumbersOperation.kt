package contacts.core.entities.operation

import android.content.ContentProviderOperation
import contacts.core.BlockedNumbersFields
import contacts.core.entities.NewBlockedNumber
import contacts.core.entities.table.Table

private val TABLE = Table.BlockedNumbers

/**
 * Builds [ContentProviderOperation]s for [Table.BlockedNumbers].
 */
internal class BlockedNumbersOperation {

    fun insert(blockedNumber: NewBlockedNumber): ContentProviderOperation? =
        if (blockedNumber.number.isNullOrBlank()) { // The number is mandatory
            null
        } else {
            // callerIsSyncAdapter only applies to ContactsContact tables. The value passed here
            // should not matter.
            newInsert(TABLE, callerIsSyncAdapter = false)
                .withValue(BlockedNumbersFields.Number, blockedNumber.number)
                .withValue(BlockedNumbersFields.NormalizedNumber, blockedNumber.normalizedNumber)
                .build()
        }
}