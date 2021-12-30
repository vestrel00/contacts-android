package contacts.entities.custom.handlename

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataEntityMapper

internal class HandleNameMapperFactory :
    AbstractCustomDataEntityMapper.Factory<HandleNameField, HandleNameDataCursor, HandleName> {

    override fun create(
        cursor: Cursor, includeFields: Set<HandleNameField>
    ): AbstractCustomDataEntityMapper<HandleNameField, HandleNameDataCursor, HandleName> =
        HandleNameMapper(HandleNameDataCursor(cursor, includeFields))
}

private class HandleNameMapper(cursor: HandleNameDataCursor) :
    AbstractCustomDataEntityMapper<HandleNameField, HandleNameDataCursor, HandleName>(cursor) {

    override fun value(cursor: HandleNameDataCursor) = HandleName(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        handle = cursor.handle,

        isRedacted = false
    )
}