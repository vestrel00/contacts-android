package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Im
import com.vestrel00.contacts.entities.cursor.ImCursor

internal class ImMapper(private val imCursor: ImCursor) : EntityMapper<Im> {

    override val value: Im
        get() = Im(
            id = imCursor.id,
            rawContactId = imCursor.rawContactId,
            contactId = imCursor.contactId,

            isPrimary = imCursor.isPrimary,
            isSuperPrimary = imCursor.isSuperPrimary,

            protocol = imCursor.protocol,
            customProtocol = imCursor.customProtocol,

            data = imCursor.data
        )
}
