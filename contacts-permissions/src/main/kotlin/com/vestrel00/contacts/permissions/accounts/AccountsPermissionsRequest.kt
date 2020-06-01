package com.vestrel00.contacts.permissions.accounts

import android.app.Activity
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.accounts.*
import com.vestrel00.contacts.permissions.R
import com.vestrel00.contacts.permissions.requestPermission
import com.vestrel00.contacts.permissions.requestReadPermission
import com.vestrel00.contacts.permissions.requestWritePermission

/**
 * If [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and [ContactsPermissions.READ_PERMISSION] are
 * not yet granted, suspends the current coroutine, requests for the permission, and then returns a
 * new [AccountsQuery] instance.
 *
 * If permissions are already granted, then immediately returns a new [AccountsQuery] instance.
 */
suspend fun Accounts.queryWithPermission(activity: Activity): AccountsQuery {
    val permissions = permissions(activity)
    if (!permissions.canQueryAccounts()) {
        requestQueryAccountsPermission(activity)
    }

    return query(activity)
}

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [AccountsRawContactsQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [AccountsRawContactsQuery]
 * instance.
 */
suspend fun Accounts.queryRawContactsWithPermission(activity: Activity): AccountsRawContactsQuery {
    val permissions = permissions(activity)
    if (!permissions.canQueryRawContacts()) {
        requestQueryRawContactsPermission(activity)
    }

    return queryRawContacts(activity)
}

/**
 * If [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and [ContactsPermissions.WRITE_PERMISSION] are
 * not yet granted, suspends the current coroutine, requests for the permission, and then returns a
 * new [AccountsRawContactsAssociationsUpdate] instance.
 *
 * If permissions are already granted, then immediately returns a new
 * [AccountsRawContactsAssociationsUpdate] instance.
 */
suspend fun Accounts.updateRawContactsAssociationsWithPermission(activity: Activity):
        AccountsRawContactsAssociationsUpdate {
    val permissions = permissions(activity)
    if (!permissions.canUpdateRawContactsAssociations()) {
        requestUpdateRawContactsAssociationsPermission(activity)
    }

    return updateRawContactsAssociations(activity)
}

/**
 * Requests the [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
 * [ContactsPermissions.READ_PERMISSION]. The current coroutine is suspended until the user either
 * grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestQueryAccountsPermission(activity: Activity): Boolean =
    requestGetAccountsPermission(activity) && requestReadPermission(activity)

/**
 * Requests the [ContactsPermissions.READ_PERMISSION]. The current coroutine is suspended until
 * the user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestQueryRawContactsPermission(activity: Activity): Boolean =
    requestReadPermission(activity)

/**
 * Requests the [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
 * [ContactsPermissions.WRITE_PERMISSION]. The current coroutine is suspended until the user either
 * grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestUpdateRawContactsAssociationsPermission(activity: Activity): Boolean =
    requestGetAccountsPermission(activity) && requestWritePermission(activity)

/**
 * Requests the [AccountsPermissions.GET_ACCOUNTS_PERMISSION]. The current coroutine is suspended
 * until the user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestGetAccountsPermission(activity: Activity): Boolean =
    requestPermission(
        AccountsPermissions.GET_ACCOUNTS_PERMISSION,
        activity,
        R.string.contacts_accounts_request_permission_title,
        R.string.contacts_accounts_request_permission_description
    )