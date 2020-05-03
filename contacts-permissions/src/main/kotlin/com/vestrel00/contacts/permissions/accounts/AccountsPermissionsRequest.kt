package com.vestrel00.contacts.permissions.accounts

import android.app.Activity
import com.vestrel00.contacts.accounts.Accounts
import com.vestrel00.contacts.accounts.AccountsPermissions
import com.vestrel00.contacts.accounts.AccountsQuery
import com.vestrel00.contacts.permissions.R
import com.vestrel00.contacts.permissions.requestPermission

/**
 * If [AccountsPermissions.GET_ACCOUNTS_PERMISSION] is not yet granted, suspends the current
 * coroutine, requests for the permission, and then returns a new [AccountsQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [AccountsQuery] instance.
 */
suspend fun Accounts.queryWithPermission(activity: Activity): AccountsQuery {
    val permissions = permissions(activity)
    if (!permissions.canGetAccounts()) {
        requestGetAccountsPermission(activity)
    }

    return query(activity)
}

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