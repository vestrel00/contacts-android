package com.vestrel00.contacts.permissions.data

import android.app.Activity
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.data.Data
import com.vestrel00.contacts.data.DataDelete
import com.vestrel00.contacts.data.DataQuery
import com.vestrel00.contacts.data.DataUpdate
import com.vestrel00.contacts.permissions.requestQueryPermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [DataQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [DataQuery] instance.
 */
suspend fun Data.queryWithPermission(activity: Activity): DataQuery {
    val permissions = permissions(activity)
    if (!permissions.canQuery()) {
        requestQueryPermission(activity)
    }

    return query(activity)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permissions, and then returns a new
 * [DataUpdate] instance.
 *
 * If permissions are already granted, then immediately returns a new [DataUpdate] instance.
 */
suspend fun Data.updateWithPermission(activity: Activity): DataUpdate {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        requestQueryPermission(activity)
    }

    return update(activity)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permissions, and then returns a new
 * [DataDelete] instance.
 *
 * If permissions are already granted, then immediately returns a new [DataDelete] instance.
 */
suspend fun Data.deleteWithPermission(activity: Activity): DataDelete {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        requestQueryPermission(activity)
    }

    return delete(activity)
}