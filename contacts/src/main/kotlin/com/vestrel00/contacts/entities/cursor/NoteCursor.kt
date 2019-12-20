package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields

/**
 * Retrieves [Fields.Note] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class NoteCursor(cursor: Cursor) : DataCursor(cursor) {

    val note: String?
        get() = cursor.getString(Fields.Note.Note)
}
