package contacts.core.entities.operation

import android.content.ContentProviderOperation
import contacts.core.BlockedNumbersFields
import contacts.core.`in`
import contacts.core.entities.NewBlockedNumber
import contacts.core.entities.table.Table
import contacts.core.equalTo

private val TABLE = Table.BlockedNumbers

/**
 * Builds [ContentProviderOperation]s for [Table.BlockedNumbers].
 */
internal class BlockedNumbersOperation {

    fun insert(blockedNumber: NewBlockedNumber): ContentProviderOperation? =
        if (blockedNumber.number.isNullOrBlank()) { // The number is mandatory
            null
        } else {
            newInsert(TABLE)
                .withValue(BlockedNumbersFields.Number, blockedNumber.number)
                .withValue(BlockedNumbersFields.NormalizedNumber, blockedNumber.normalizedNumber)
                .build()
        }

    fun delete(blockedNumberId: Long): ContentProviderOperation = newDelete(TABLE)
        .withSelection(BlockedNumbersFields.Id equalTo blockedNumberId)
        .build()

    fun delete(blockedNumberIds: Collection<Long>): ContentProviderOperation = newDelete(TABLE)
        .withSelection(BlockedNumbersFields.Id `in` blockedNumberIds)
        .build()
}