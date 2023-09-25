package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.GroupMembershipField

/**
 * Retrieves [Fields.GroupMembership] data from the given [cursor].
 */
internal class GroupMembershipCursor(cursor: Cursor, includeFields: Set<GroupMembershipField>?) :
    AbstractDataCursor<GroupMembershipField>(cursor, includeFields) {

    val groupId: Long? by long(Fields.GroupMembership.GroupId)
}
