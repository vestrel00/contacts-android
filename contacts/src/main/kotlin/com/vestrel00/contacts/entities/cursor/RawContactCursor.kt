package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.INVALID_ID

/**
 * Retrieves [Fields.RawContactId] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class RawContactCursor(private val cursor: Cursor) {

    val id: Long
        get() = cursor.getLong(Fields.RawContactId) ?: INVALID_ID

    val contactId: Long
        get() = cursor.getLong(Fields.Contact.Id) ?: INVALID_ID
}
