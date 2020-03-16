package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableNote
import com.vestrel00.contacts.entities.Note
import com.vestrel00.contacts.entities.cursor.NoteCursor

internal class NoteMapper(private val noteCursor: NoteCursor) : EntityMapper<Note, MutableNote> {

    override val toImmutable: Note
        get() = Note(
            id = noteCursor.id,
            rawContactId = noteCursor.rawContactId,
            contactId = noteCursor.contactId,

            isPrimary = noteCursor.isPrimary,
            isSuperPrimary = noteCursor.isSuperPrimary,

            note = noteCursor.note
        )

    override val toMutable: MutableNote
        get() = MutableNote(
            id = noteCursor.id,
            rawContactId = noteCursor.rawContactId,
            contactId = noteCursor.contactId,

            isPrimary = noteCursor.isPrimary,
            isSuperPrimary = noteCursor.isSuperPrimary,

            note = noteCursor.note
        )
}
