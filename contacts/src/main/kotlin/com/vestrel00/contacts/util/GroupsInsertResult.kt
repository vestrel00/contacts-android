package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.MutableGroup
import com.vestrel00.contacts.groups.GroupsInsert
import com.vestrel00.contacts.groups.GroupsQuery

fun GroupsInsert.Result.group(
    context: Context, group: MutableGroup, cancel: () -> Boolean = { false }
): Group? {

    val groupId = groupId(group) ?: return null

    return GroupsQuery(context).withIds(groupId).findFirst(cancel)
}

fun GroupsInsert.Result.groups(context: Context, cancel: () -> Boolean = { false }): List<Group> {

    if (groupIds.isEmpty()) {
        return emptyList()
    }

    return GroupsQuery(context).withIds(groupIds).find(cancel)
}