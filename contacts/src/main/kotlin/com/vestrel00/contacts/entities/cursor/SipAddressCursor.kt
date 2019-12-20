package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields

/**
 * Retrieves [Fields.SipAddress] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class SipAddressCursor(cursor: Cursor) : DataCursor(cursor) {

    val sipAddress: String?
        get() = cursor.getString(Fields.SipAddress.SipAddress)
}
