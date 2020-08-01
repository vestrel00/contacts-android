package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Photo
import com.vestrel00.contacts.entities.cursor.PhotoCursor

internal class PhotoMapper(private val phoneCursor: PhotoCursor) : EntityMapper<Photo> {

    override val value: Photo
        get() = Photo(
            id = phoneCursor.dataId,
            rawContactId = phoneCursor.rawContactId,
            contactId = phoneCursor.contactId,

            isPrimary = phoneCursor.isPrimary,
            isSuperPrimary = phoneCursor.isSuperPrimary
        )
}
