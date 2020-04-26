package com.vestrel00.contacts.entities.cursor

import android.annotation.TargetApi
import android.database.Cursor
import android.net.Uri
import android.os.Build
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.INVALID_ID
import java.util.*

/**
 * Retrieves [Fields.Contacts] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class ContactsCursor(private val cursor: Cursor) : ContactIdCursor {

    override val contactId: Long
        get() = cursor.getLong(Fields.Contacts.Id) ?: INVALID_ID

    val displayName: String?
        get() = cursor.getString(Fields.Contacts.DisplayName)

    val displayNameSource: Int?
        get() = cursor.getInt(Fields.Contacts.DisplayNameSource)

    val nameRawContactId: Long?
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        get() = cursor.getLong(Fields.Contacts.NameRawContactId)

    val lastUpdatedTimestamp: Date?
        get() = cursor.getDate(Fields.Contacts.LastUpdatedTimestamp)

    val photoUri: Uri?
        get() = cursor.getUri(Fields.Contacts.PhotoUri)

    val photoThumbnailUri: Uri?
        get() = cursor.getUri(Fields.Contacts.PhotoThumbnailUri)

    val photoFileId: Long?
        get() {
            val value = cursor.getLong(Fields.Contacts.PhotoFileId)
            // Sometimes the value will be zero instead of null but 0 is not a valid photo file id.
            return if (value != null && value > 0) value else null
        }
}
