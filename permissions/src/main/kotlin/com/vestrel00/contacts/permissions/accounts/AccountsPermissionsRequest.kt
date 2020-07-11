package com.vestrel00.contacts.permissions.accounts

import android.content.Context
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
suspend fun Accounts.queryWithPermission(context: Context): AccountsQuery {
    val permissions = permissions(context)
    if (!permissions.canQueryAccounts()) {
        requestQueryAccountsPermission(context)
    }

    return query(context)
}

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [AccountsRawContactsQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [AccountsRawContactsQuery]
 * instance.
 */
suspend fun Accounts.queryRawContactsWithPermission(context: Context): AccountsRawContactsQuery {
    val permissions = permissions(context)
    if (!permissions.canQueryRawContacts()) {
        requestQueryRawContactsPermission(context)
    }

    return queryRawContacts(context)
}

/**
 * If [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and [ContactsPermissions.WRITE_PERMISSION] are
 * not yet granted, suspends the current coroutine, requests for the permission, and then returns a
 * new [AccountsRawContactsAssociationsUpdate] instance.
 *
 * If permissions are already granted, then immediately returns a new
 * [AccountsRawContactsAssociationsUpdate] instance.
 */
suspend fun Accounts.updateRawContactsAssociationsWithPermission(context: Context):
        AccountsRawContactsAssociationsUpdate {
    val permissions = permissions(context)
    if (!permissions.canUpdateRawContactsAssociations()) {
        requestUpdateRawContactsAssociationsPermission(context)
    }

    return updateRawContactsAssociations(context)
}

/**
 * Requests the [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
 * [ContactsPermissions.READ_PERMISSION]. The current coroutine is suspended until the user either
 * grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestQueryAccountsPermission(context: Context): Boolean =
    requestGetAccountsPermission(context) && requestReadPermission(context)

/**
 * Requests the [ContactsPermissions.READ_PERMISSION]. The current coroutine is suspended until
 * the user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestQueryRawContactsPermission(context: Context): Boolean =
    requestReadPermission(context)

/**
 * Requests the [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
 * [ContactsPermissions.WRITE_PERMISSION]. The current coroutine is suspended until the user either
 * grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestUpdateRawContactsAssociationsPermission(context: Context): Boolean =
    requestGetAccountsPermission(context) && requestWritePermission(context)

/**
 * Requests the [AccountsPermissions.GET_ACCOUNTS_PERMISSION]. The current coroutine is suspended
 * until the user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestGetAccountsPermission(context: Context): Boolean =
    requestPermission(
        AccountsPermissions.GET_ACCOUNTS_PERMISSION,
        context,
        R.string.contacts_accounts_request_permission_title,
        R.string.contacts_accounts_request_permission_description
    )