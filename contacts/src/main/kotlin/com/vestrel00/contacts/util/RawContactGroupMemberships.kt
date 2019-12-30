package com.vestrel00.contacts.util

import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.GroupMembership
import com.vestrel00.contacts.entities.INVALID_ID

fun Group.toGroupMembership(): GroupMembership = GroupMembership(
    id = INVALID_ID,
    rawContactId = INVALID_ID,
    contactId = INVALID_ID,
    groupId = id
)

fun Collection<Group>.toGroupMemberships(): List<GroupMembership> = map { it.toGroupMembership() }

fun Collection<Group>.defaultGroup(): Group? = firstOrNull { it.isDefaultGroup }

fun Collection<Group>.favoritesGroup(): Group? = firstOrNull { it.isFavoritesGroup }