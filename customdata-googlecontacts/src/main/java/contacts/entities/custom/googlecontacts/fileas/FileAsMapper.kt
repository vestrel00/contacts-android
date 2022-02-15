package contacts.entities.custom.googlecontacts.fileas

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataEntityMapper
import contacts.entities.custom.googlecontacts.FileAsField

internal class FileAsMapperFactory :
    AbstractCustomDataEntityMapper.Factory<FileAsField, FileAsDataCursor, FileAs> {

    override fun create(
        cursor: Cursor, includeFields: Set<FileAsField>
    ): AbstractCustomDataEntityMapper<FileAsField, FileAsDataCursor, FileAs> =
        FileAsMapper(FileAsDataCursor(cursor, includeFields))
}

private class FileAsMapper(cursor: FileAsDataCursor) :
    AbstractCustomDataEntityMapper<FileAsField, FileAsDataCursor, FileAs>(cursor) {

    override fun value(cursor: FileAsDataCursor) = FileAs(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        name = cursor.name,

        isRedacted = false
    )
}