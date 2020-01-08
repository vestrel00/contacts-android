package com.vestrel00.contacts.permissions.accounts

import android.accounts.Account
import android.app.Activity
import com.vestrel00.contacts.accounts.Accounts
import com.vestrel00.contacts.accounts.AccountsPermissions
import com.vestrel00.contacts.permissions.R
import com.vestrel00.contacts.permissions.requestPermission

/**
 * If [AccountsPermissions.GET_ACCOUNTS_PERMISSION] is not yet granted, suspends the current
 * coroutine, requests for the permission, and then returns the list of all available [Account]s.
 *
 * If permission is already granted, then immediately returns the list of all available [Account]s.
 */
suspend fun Accounts.allAccountsWithPermission(activity: Activity): List<Account> =
    accountsWithPermission(activity) { allAccounts(activity) }

/**
 * If [AccountsPermissions.GET_ACCOUNTS_PERMISSION] is not yet granted, suspends the current
 * coroutine, requests for the permission, and then returns all available [Account]s with the given
 * [Account.type].
 *
 * If permission is already granted, then immediately returns the list of all available [Account]s.
 */
suspend fun Accounts.accountsWithTypeWithPermission(
    activity: Activity, type: String
): List<Account> = accountsWithPermission(activity) { accountsWithType(activity, type) }

private suspend inline fun Accounts.accountsWithPermission(
    activity: Activity, accounts: () -> List<Account>
): List<Account> {
    val permissions = permissions(activity)
    if (!permissions.canGetAccounts()) {
        permissions.requestGetAccountsPermission(activity)
    }

    return accounts()
}

/**
 * Requests the [AccountsPermissions.GET_ACCOUNTS_PERMISSION]. The current coroutine is suspended
 * until the user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun AccountsPermissions.requestGetAccountsPermission(activity: Activity): Boolean =
    requestPermission(
        AccountsPermissions.GET_ACCOUNTS_PERMISSION,
        activity,
        R.string.contacts_accounts_request_permission_title,
        R.string.contacts_accounts_request_permission_description
    )