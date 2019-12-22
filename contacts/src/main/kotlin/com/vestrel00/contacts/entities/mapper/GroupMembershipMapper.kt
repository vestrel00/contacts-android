package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.GroupMembership
import com.vestrel00.contacts.entities.cursor.GroupMembershipCursor

internal class GroupMembershipMapper(private val groupMembershipCursor: GroupMembershipCursor) {

    val groupMembership: GroupMembership
        get() = GroupMembership(
            id = groupMembershipCursor.id,
            rawContactId = groupMembershipCursor.rawContactId,
            contactId = groupMembershipCursor.contactId,

            groupId = groupMembershipCursor.groupId
        )
}
