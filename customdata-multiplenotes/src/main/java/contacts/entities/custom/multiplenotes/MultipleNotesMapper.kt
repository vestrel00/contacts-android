package contacts.entities.custom.multiplenotes

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataEntityMapper

internal class MultipleNotesMapperFactory :
    AbstractCustomDataEntityMapper.Factory<MultipleNotesField, MultipleNotesDataCursor, MultipleNotes> {

    override fun create(
        cursor: Cursor, includeFields: Set<MultipleNotesField>?
    ): AbstractCustomDataEntityMapper<MultipleNotesField, MultipleNotesDataCursor, MultipleNotes> =
        MultipleNotesMapper(MultipleNotesDataCursor(cursor, includeFields))
}

private class MultipleNotesMapper(cursor: MultipleNotesDataCursor) :
    AbstractCustomDataEntityMapper<MultipleNotesField, MultipleNotesDataCursor, MultipleNotes>(
        cursor
    ) {

    override fun value(cursor: MultipleNotesDataCursor) = MultipleNotes(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        note = cursor.note,

        isRedacted = false
    )
}