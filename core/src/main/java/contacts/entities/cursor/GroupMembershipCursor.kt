package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.GroupMembershipField

/**
 * Retrieves [Fields.GroupMembership] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class GroupMembershipCursor(cursor: Cursor) :
    AbstractDataCursor<GroupMembershipField>(cursor) {

    val groupId: Long?
        get() = getLong(Fields.GroupMembership.GroupId)
}
