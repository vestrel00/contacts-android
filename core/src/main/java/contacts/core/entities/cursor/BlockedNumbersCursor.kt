package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.BlockedNumbersField
import contacts.core.BlockedNumbersFields
import contacts.core.entities.Entity

/**
 * Retrieves [BlockedNumbersFields] data from the given [cursor].
 */
internal class BlockedNumbersCursor(cursor: Cursor, includeFields: Set<BlockedNumbersField>?) :
    AbstractEntityCursor<BlockedNumbersField>(cursor, includeFields) {

    val id: Long by nonNullLong(BlockedNumbersFields.Id, Entity.INVALID_ID)

    val number: String? by string(BlockedNumbersFields.Number)

    val normalizedNumber: String? by string(BlockedNumbersFields.NormalizedNumber)
}
