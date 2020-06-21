package com.vestrel00.contacts.entities.cursor

import android.annotation.TargetApi
import android.database.Cursor
import android.net.Uri
import android.os.Build
import com.vestrel00.contacts.ContactsFields
import java.util.*

/**
 * Retrieves [ContactsFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class ContactsCursor(private val cursor: Cursor) : ContactIdCursor {

    override val contactId: Long?
        get() = cursor.getLong(ContactsFields.Id)

    val displayNamePrimary: String?
        get() = cursor.getString(ContactsFields.DisplayNamePrimary)

    val displayNameSource: Int?
        get() = cursor.getInt(ContactsFields.DisplayNameSource)

    val nameRawContactId: Long?
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        get() = cursor.getLong(ContactsFields.NameRawContactId)

    val lastUpdatedTimestamp: Date?
        get() = cursor.getDate(ContactsFields.LastUpdatedTimestamp)

    val photoUri: Uri?
        get() = cursor.getUri(ContactsFields.PhotoUri)

    val photoThumbnailUri: Uri?
        get() = cursor.getUri(ContactsFields.PhotoThumbnailUri)

    val photoFileId: Long?
        get() {
            val value = cursor.getLong(ContactsFields.PhotoFileId)
            // Sometimes the value will be zero instead of null but 0 is not a valid photo file id.
            return if (value != null && value > 0) value else null
        }
}
