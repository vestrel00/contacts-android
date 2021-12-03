package contacts.entities.custom.handlename

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomEntityMapper

internal class HandleNameMapperFactory :
    AbstractCustomEntityMapper.Factory<HandleNameField, HandleNameDataCursor, HandleNameEntity> {

    override fun create(
        cursor: Cursor, includeFields: Set<HandleNameField>
    ): AbstractCustomEntityMapper<HandleNameField, HandleNameDataCursor, HandleNameEntity> =
        HandleNameMapper(HandleNameDataCursor(cursor, includeFields))
}

private class HandleNameMapper(cursor: HandleNameDataCursor) :
    AbstractCustomEntityMapper<HandleNameField, HandleNameDataCursor, HandleName>(cursor) {

    override fun value(cursor: HandleNameDataCursor) = HandleName(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        handle = cursor.handle
    )
}