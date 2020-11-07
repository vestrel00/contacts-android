package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Note
import com.vestrel00.contacts.entities.cursor.NoteCursor

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
