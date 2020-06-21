package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields
import java.util.*

/**
 * Retrieves [Fields.Contact] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class ContactCursor(private val cursor: Cursor) : ContactIdCursor {

    override val contactId: Long?
        get() = cursor.getLong(Fields.Contact.Id)

    val displayNamePrimary: String?
        get() = cursor.getString(Fields.Contact.DisplayNamePrimary)

    val lastUpdatedTimestamp: Date?
        get() = cursor.getDate(Fields.Contact.LastUpdatedTimestamp)
}
