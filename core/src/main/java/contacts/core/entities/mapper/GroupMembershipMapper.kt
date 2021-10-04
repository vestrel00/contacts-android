package contacts.core.entities.mapper

import contacts.core.entities.GroupMembership
import contacts.core.entities.cursor.GroupMembershipCursor

internal class GroupMembershipMapper(private val groupMembershipCursor: GroupMembershipCursor) :
    EntityMapper<GroupMembership> {

    override val value: GroupMembership
        get() = GroupMembership(
            id = groupMembershipCursor.dataId,
            rawContactId = groupMembershipCursor.rawContactId,
            contactId = groupMembershipCursor.contactId,

            isPrimary = groupMembershipCursor.isPrimary,
            isSuperPrimary = groupMembershipCursor.isSuperPrimary,

            groupId = groupMembershipCursor.groupId
        )
}
