package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableEmail
import com.vestrel00.contacts.entities.cursor.EmailCursor

internal class EmailMapper(private val emailCursor: EmailCursor) {

    val email: MutableEmail
        get() = MutableEmail(
            id = emailCursor.id,
            rawContactId = emailCursor.rawContactId,
            contactId = emailCursor.contactId,

            type = emailCursor.type,
            label = emailCursor.label,

            address = emailCursor.address
        )
}
