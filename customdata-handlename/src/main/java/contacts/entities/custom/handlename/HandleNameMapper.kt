package contacts.entities.custom.handlename

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomEntityMapper

internal class HandleNameMapperFactory :
    AbstractCustomEntityMapper.Factory<HandleNameField, HandleNameDataCursor, MutableHandleName> {

    override fun create(
        cursor: Cursor, includeFields: Set<HandleNameField>
    ): AbstractCustomEntityMapper<HandleNameField, HandleNameDataCursor, MutableHandleName> =
        HandleNameMapper(HandleNameDataCursor(cursor, includeFields))
}

private class HandleNameMapper(cursor: HandleNameDataCursor) :
    AbstractCustomEntityMapper<HandleNameField, HandleNameDataCursor, MutableHandleName>(cursor) {

    override fun value(cursor: HandleNameDataCursor) = MutableHandleName(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        handle = cursor.handle
    )
}