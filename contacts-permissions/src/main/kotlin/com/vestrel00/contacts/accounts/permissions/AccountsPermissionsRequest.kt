package com.vestrel00.contacts.accounts.permissions

import android.accounts.Account
import android.app.Activity
import com.vestrel00.contacts.accounts.Accounts
import com.vestrel00.contacts.accounts.AccountsPermissions
import com.vestrel00.contacts.permissions.R
import com.vestrel00.contacts.permissions.requestPermission

suspend fun Accounts.allAccountsWithPermission(activity: Activity): List<Account> =
    accountsWithPermission(activity) { allAccounts(activity) }

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

suspend fun AccountsPermissions.requestGetAccountsPermission(activity: Activity): Boolean =
    requestPermission(
        AccountsPermissions.GET_ACCOUNTS_PERMISSION,
        activity,
        R.string.contacts_accounts_request_permission_title,
        R.string.contacts_accounts_request_permission_description
    )