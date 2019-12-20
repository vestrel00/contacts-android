package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.Phone

/**
 * Retrieves [Fields.Phone] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class PhoneCursor(cursor: Cursor) : DataCursor(cursor) {

    val type: Phone.Type
        get() = Phone.Type.fromValue(cursor.getInt(Fields.Phone.Type))

    val label: String?
        get() = cursor.getString(Fields.Phone.Label)

    val number: String?
        get() = cursor.getString(Fields.Phone.Number)

    val normalizedNumber: String?
        get() = cursor.getString(Fields.Phone.NormalizedNumber)
}
