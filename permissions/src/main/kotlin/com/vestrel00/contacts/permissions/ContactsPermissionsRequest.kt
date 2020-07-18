package com.vestrel00.contacts.permissions

import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.permissions.accounts.requestGetAccountsPermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [Query] instance.
 *
 * If permission is already granted, then immediately returns a new [Query] instance.
 */
suspend fun Contacts.queryWithPermission(context: Context): Query {
    val permissions = permissions(context)
    if (!permissions.canQuery()) {
        requestReadPermission(context)
    }

    return query(context)
}

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [GeneralQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [GeneralQuery] instance.
 */
suspend fun Contacts.generalQueryWithPermission(context: Context): GeneralQuery {
    val permissions = permissions(context)
    if (!permissions.canQuery()) {
        requestReadPermission(context)
    }

    return generalQuery(context)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permissions, and then returns a new
 * [Insert] instance.
 *
 * If permissions are already granted, then immediately returns a new [Insert] instance.
 */
suspend fun Contacts.insertWithPermission(context: Context): Insert {
    val permissions = permissions(context)
    if (!permissions.canInsertUpdateDelete()) {
        requestWritePermission(context)
        requestGetAccountsPermission(context)
    }

    return insert(context)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permissions, and then returns a new
 * [Update] instance.
 *
 * If permissions are already granted, then immediately returns a new [Update] instance.
 */
suspend fun Contacts.updateWithPermission(context: Context): Update {
    val permissions = permissions(context)
    if (!permissions.canInsertUpdateDelete()) {
        requestWritePermission(context)
        requestGetAccountsPermission(context)
    }

    return update(context)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [Delete] instance.
 *
 * If permission is already granted, then immediately returns a new [Delete] instance.
 */
suspend fun Contacts.deleteWithPermission(context: Context): Delete {
    val permissions = permissions(context)
    if (!permissions.canInsertUpdateDelete()) {
        requestWritePermission(context)
        requestGetAccountsPermission(context)
    }

    return delete(context)
}

/**
 * Requests the [ContactsPermissions.READ_PERMISSION]. The current coroutine is suspended until the
 * user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestReadPermission(context: Context): Boolean =
    requestContactsPermission(ContactsPermissions.READ_PERMISSION, context)

/**
 * Requests the [ContactsPermissions.WRITE_PERMISSION]. The current coroutine is suspended until
 * the user either grants or denies the permissions request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestWritePermission(context: Context): Boolean =
    requestContactsPermission(ContactsPermissions.WRITE_PERMISSION, context)

private suspend fun requestContactsPermission(permission: String, context: Context): Boolean =
    requestPermission(
        permission,
        context,
        R.string.contacts_request_permission_title,
        R.string.contacts_request_permission_description
    )