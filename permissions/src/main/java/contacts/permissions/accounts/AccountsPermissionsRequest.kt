package contacts.permissions.accounts

import contacts.core.accounts.Accounts
import contacts.core.accounts.AccountsPermissions
import contacts.core.accounts.AccountsQuery
import contacts.core.accounts.MoveRawContactsAcrossAccounts
import contacts.permissions.R
import contacts.permissions.requestPermission
import contacts.permissions.requestReadPermission
import contacts.permissions.requestWritePermission

/**
 * If [AccountsPermissions.GET_ACCOUNTS_PERMISSION] is not yet granted, suspends the current
 * coroutine, requests for the permission, and then returns a new [AccountsQuery] instance.
 *
 * If permissions are already granted, then immediately returns a new [AccountsQuery] instance.
 */
suspend fun Accounts.queryWithPermission(): AccountsQuery {
    if (!contactsApi.accountsPermissions.canQueryAccounts()) {
        requestQueryAccountsPermission()
    }

    return query()
}

/**
 * If [AccountsPermissions.GET_ACCOUNTS_PERMISSION], [contacts.core.ContactsPermissions.READ_PERMISSION], and
 * [contacts.core.ContactsPermissions.WRITE_PERMISSION] are not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [MoveRawContactsAcrossAccounts] instance.
 *
 * If permissions are already granted, then immediately returns a new
 * [MoveRawContactsAcrossAccounts] instance.
 */
suspend fun Accounts.moveWithPermission(): MoveRawContactsAcrossAccounts {
    if (!contactsApi.accountsPermissions.canMoveRawContactsAcrossAccounts()) {
        requestMoveRawContactsAcrossAccountsPermission()
    }

    return move()
}

/**
 * Requests the [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
 * [contacts.core.ContactsPermissions.READ_PERMISSION]. The current coroutine is suspended until the user either
 * grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestQueryAccountsPermission(): Boolean =
    requestGetAccountsPermission() && requestReadPermission()

/**
 * Requests the [AccountsPermissions.GET_ACCOUNTS_PERMISSION],
 * [contacts.core.ContactsPermissions.READ_PERMISSION], and
 * [contacts.core.ContactsPermissions.WRITE_PERMISSION]. The current
 * coroutine is suspended until the user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestMoveRawContactsAcrossAccountsPermission(): Boolean =
    requestGetAccountsPermission() && requestReadPermission() && requestWritePermission()

/**
 * Requests the [AccountsPermissions.GET_ACCOUNTS_PERMISSION]. The current coroutine is suspended
 * until the user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestGetAccountsPermission(): Boolean =
    requestPermission(
        AccountsPermissions.GET_ACCOUNTS_PERMISSION,
        R.string.contacts_accounts_request_permission_title,
        R.string.contacts_accounts_request_permission_description
    )