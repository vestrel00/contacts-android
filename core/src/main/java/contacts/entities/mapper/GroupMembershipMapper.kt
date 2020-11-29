package contacts.entities.mapper

import contacts.entities.GroupMembership
import contacts.entities.cursor.GroupMembershipCursor

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
