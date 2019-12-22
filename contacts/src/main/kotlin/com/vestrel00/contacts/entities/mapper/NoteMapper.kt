package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableNote
import com.vestrel00.contacts.entities.cursor.NoteCursor

internal class NoteMapper(private val noteCursor: NoteCursor) {

    val note: MutableNote
        get() = MutableNote(
            id = noteCursor.id,
            rawContactId = noteCursor.rawContactId,
            contactId = noteCursor.contactId,

            note = noteCursor.note
        )
}
