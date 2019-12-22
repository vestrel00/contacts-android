package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.cursor.MimeTypeCursor

internal class MimeTypeMapper(private val mimeTypeCursor: MimeTypeCursor) {

    val mimeType: MimeType
        get() = mimeTypeCursor.mimeType
}
