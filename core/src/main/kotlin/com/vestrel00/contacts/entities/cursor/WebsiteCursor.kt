package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields

/**
 * Retrieves [Fields.Website] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class WebsiteCursor(cursor: Cursor) : DataCursor(cursor) {

    val url: String?
        get() = cursor.getString(Fields.Website.Url)
}
