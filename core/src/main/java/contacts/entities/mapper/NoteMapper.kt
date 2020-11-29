package contacts.entities.mapper

import contacts.entities.Note
import contacts.entities.cursor.NoteCursor

internal class NoteMapper(private val noteCursor: NoteCursor) : EntityMapper<Note> {

    override val value: Note
        get() = Note(
            id = noteCursor.dataId,
            rawContactId = noteCursor.rawContactId,
            contactId = noteCursor.contactId,

            isPrimary = noteCursor.isPrimary,
            isSuperPrimary = noteCursor.isSuperPrimary,

            note = noteCursor.note
        )
}
