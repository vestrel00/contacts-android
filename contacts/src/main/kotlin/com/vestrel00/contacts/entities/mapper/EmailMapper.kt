package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Email
import com.vestrel00.contacts.entities.MutableEmail
import com.vestrel00.contacts.entities.cursor.EmailCursor

internal class EmailMapper(private val emailCursor: EmailCursor) :
    EntityMapper<Email, MutableEmail> {

    override val toImmutable: Email
        get() = Email(
            id = emailCursor.id,
            rawContactId = emailCursor.rawContactId,
            contactId = emailCursor.contactId,

            isPrimary = emailCursor.isPrimary,
            isSuperPrimary = emailCursor.isSuperPrimary,

            type = emailCursor.type,
            label = emailCursor.label,

            address = emailCursor.address
        )

    override val toMutable: MutableEmail
        get() = MutableEmail(
            id = emailCursor.id,
            rawContactId = emailCursor.rawContactId,
            contactId = emailCursor.contactId,

            isPrimary = emailCursor.isPrimary,
            isSuperPrimary = emailCursor.isSuperPrimary,

            type = emailCursor.type,
            label = emailCursor.label,

            address = emailCursor.address
        )
}
