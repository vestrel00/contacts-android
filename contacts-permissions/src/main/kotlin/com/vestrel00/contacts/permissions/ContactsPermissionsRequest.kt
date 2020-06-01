package com.vestrel00.contacts.permissions

import android.app.Activity
import com.vestrel00.contacts.*
import com.vestrel00.contacts.permissions.accounts.requestGetAccountsPermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [Query] instance.
 *
 * If permission is already granted, then immediately returns a new [Query] instance.
 */
suspend fun Contacts.queryWithPermission(activity: Activity): Query {
    val permissions = permissions(activity)
    if (!permissions.canQuery()) {
        requestReadPermission(activity)
    }

    return query(activity)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permissions, and then returns a new
 * [Insert] instance.
 *
 * If permissions are already granted, then immediately returns a new [Insert] instance.
 */
suspend fun Contacts.insertWithPermission(activity: Activity): Insert {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        requestWritePermission(activity)
        requestGetAccountsPermission(activity)
    }

    return insert(activity)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permissions, and then returns a new
 * [Update] instance.
 *
 * If permissions are already granted, then immediately returns a new [Update] instance.
 */
suspend fun Contacts.updateWithPermission(activity: Activity): Update {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        requestWritePermission(activity)
        requestGetAccountsPermission(activity)
    }

    return update(activity)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [Delete] instance.
 *
 * If permission is already granted, then immediately returns a new [Delete] instance.
 */
suspend fun Contacts.deleteWithPermission(activity: Activity): Delete {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        requestWritePermission(activity)
        requestGetAccountsPermission(activity)
    }

    return delete(activity)
}

/**
 * Requests the [ContactsPermissions.READ_PERMISSION]. The current coroutine is suspended until the
 * user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestReadPermission(activity: Activity): Boolean =
    requestContactsPermission(ContactsPermissions.READ_PERMISSION, activity)

/**
 * Requests the [ContactsPermissions.WRITE_PERMISSION]. The current coroutine is suspended until
 * the user either grants or denies the permissions request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestWritePermission(activity: Activity): Boolean =
    requestContactsPermission(ContactsPermissions.WRITE_PERMISSION, activity)

private suspend fun requestContactsPermission(permission: String, activity: Activity): Boolean =
    requestPermission(
        permission,
        activity,
        R.string.contacts_request_permission_title,
        R.string.contacts_request_permission_description
    )