package contacts.core.entities.mapper

import contacts.core.entities.Im
import contacts.core.entities.cursor.ImCursor

internal class ImMapper(private val imCursor: ImCursor) : DataEntityMapper<Im> {

    override val value: Im
        get() = Im(
            id = imCursor.dataId,
            rawContactId = imCursor.rawContactId,
            contactId = imCursor.contactId,

            isPrimary = imCursor.isPrimary,
            isSuperPrimary = imCursor.isSuperPrimary,

            protocol = imCursor.protocol,
            customProtocol = imCursor.customProtocol,

            data = imCursor.data
        )
}
