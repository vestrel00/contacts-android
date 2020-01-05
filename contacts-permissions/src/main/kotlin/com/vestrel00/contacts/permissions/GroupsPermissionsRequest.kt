package com.vestrel00.contacts.permissions

import android.app.Activity
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.groups.Groups
import com.vestrel00.contacts.groups.GroupsInsert
import com.vestrel00.contacts.groups.GroupsQuery
import com.vestrel00.contacts.groups.GroupsUpdate

suspend fun Groups.queryWithPermission(activity: Activity): GroupsQuery {
    val permissions = permissions(activity)
    if (!permissions.canQuery()) {
        permissions.requestQueryPermission(activity)
    }

    return query(activity)
}

suspend fun Groups.insertWithPermission(activity: Activity): GroupsInsert {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        permissions.requestInsertUpdateDeletePermission(activity)
    }

    return insert(activity)
}

suspend fun Groups.updateWithPermission(activity: Activity): GroupsUpdate {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        permissions.requestInsertUpdateDeletePermission(activity)
    }

    return update(activity)
}