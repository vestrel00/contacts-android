package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Email
import com.vestrel00.contacts.entities.cursor.EmailCursor

internal class EmailMapper(private val emailCursor: EmailCursor) : EntityMapper<Email> {

    override val value: Email
        get() = Email(
            id = emailCursor.dataId,
            rawContactId = emailCursor.rawContactId,
            contactId = emailCursor.contactId,

            isPrimary = emailCursor.isPrimary,
            isSuperPrimary = emailCursor.isSuperPrimary,

            type = emailCursor.type,
            label = emailCursor.label,

            address = emailCursor.address
        )
}
