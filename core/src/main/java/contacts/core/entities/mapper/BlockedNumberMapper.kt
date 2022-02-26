package contacts.core.entities.mapper

import contacts.core.entities.BlockedNumber
import contacts.core.entities.cursor.BlockedNumbersCursor

internal class BlockedNumberMapper(private val blockedNumbersCursor: BlockedNumbersCursor) :
    EntityMapper<BlockedNumber> {

    override val value: BlockedNumber
        get() = BlockedNumber(
            id = blockedNumbersCursor.id,

            number = blockedNumbersCursor.number,
            normalizedNumber = blockedNumbersCursor.normalizedNumber,

            isRedacted = false
        )
}
