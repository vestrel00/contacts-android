package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableIm
import com.vestrel00.contacts.entities.cursor.ImCursor

internal class ImMapper(private val imCursor: ImCursor) {

    val im: MutableIm
        get() = MutableIm(
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
