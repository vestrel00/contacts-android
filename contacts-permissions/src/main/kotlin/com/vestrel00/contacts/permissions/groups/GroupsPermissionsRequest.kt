package com.vestrel00.contacts.permissions.groups

import android.app.Activity
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.groups.Groups
import com.vestrel00.contacts.groups.GroupsInsert
import com.vestrel00.contacts.groups.GroupsQuery
import com.vestrel00.contacts.groups.GroupsUpdate
import com.vestrel00.contacts.permissions.requestInsertUpdateDeletePermission
import com.vestrel00.contacts.permissions.requestQueryPermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [GroupsQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [GroupsQuery] instance.
 */
suspend fun Groups.queryWithPermission(activity: Activity): GroupsQuery {
    val permissions = permissions(activity)
    if (!permissions.canQuery()) {
        permissions.requestQueryPermission(activity)
    }

    return query(activity)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permissions, and then returns a new
 * [GroupsInsert] instance.
 *
 * If permissions are already granted, then immediately returns a new [GroupsInsert] instance.
 */
suspend fun Groups.insertWithPermission(activity: Activity): GroupsInsert {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        permissions.requestInsertUpdateDeletePermission(activity)
    }

    return insert(activity)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permissions, and then returns a new
 * [GroupsUpdate] instance.
 *
 * If permissions are already granted, then immediately returns a new [GroupsUpdate] instance.
 */
suspend fun Groups.updateWithPermission(activity: Activity): GroupsUpdate {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        permissions.requestInsertUpdateDeletePermission(activity)
    }

    return update(activity)
}