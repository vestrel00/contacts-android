package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields

internal class PhotoCursor(cursor: Cursor) : DataCursor(cursor) {

    val photoFileId: Long?
        get() {
            val value = cursor.getLong(Fields.Photo.PhotoFileId)
            // Sometimes the value will be zero instead of null but 0 is not a valid photo file id.
            return if (value != null && value > 0) value else null
        }

    val photoThumbnail: ByteArray?
        get() = cursor.getBlob(Fields.Photo.PhotoThumbnail)
}
