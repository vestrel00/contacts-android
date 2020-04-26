package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.INVALID_ID

internal open class DataCursor(protected val cursor: Cursor) : DataIdCursor {

    override val dataId: Long
        get() = cursor.getLong(Fields.Id) ?: INVALID_ID

    override val rawContactId: Long
        get() = cursor.getLong(Fields.RawContact.Id) ?: INVALID_ID

    override val contactId: Long
        get() = cursor.getLong(Fields.Contact.Id) ?: INVALID_ID

    val isPrimary: Boolean
        get() = cursor.getBoolean(Fields.IsPrimary) ?: false

    val isSuperPrimary: Boolean
        get() = cursor.getBoolean(Fields.IsSuperPrimary) ?: false
}