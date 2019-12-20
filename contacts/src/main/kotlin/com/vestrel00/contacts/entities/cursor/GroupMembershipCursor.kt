package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.INVALID_ID

/**
 * Retrieves [Fields.GroupMembership] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class GroupMembershipCursor(cursor: Cursor) : DataCursor(cursor) {

    val groupId: Long
        get() = cursor.getLong(Fields.GroupMembership.GroupId) ?: INVALID_ID
}
