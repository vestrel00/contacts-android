package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.INVALID_ID

internal abstract class DataCursor(protected val cursor: Cursor) {

    val id: Long
        get() = cursor.getLong(Fields.Id) ?: INVALID_ID

    val rawContactId: Long
        get() = cursor.getLong(Fields.RawContact.Id) ?: INVALID_ID

    val contactId: Long
        get() = cursor.getLong(Fields.Contact.Id) ?: INVALID_ID

    val isPrimary: Boolean
        get() = cursor.getBoolean(Fields.IsPrimary) ?: false

    val isSuperPrimary: Boolean
        get() = cursor.getBoolean(Fields.IsSuperPrimary) ?: false
}