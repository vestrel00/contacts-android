package com.vestrel00.contacts.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

/**
 * Retrieves [Account]s from the [AccountManager].
 *
 * ## Permissions
 *
 * The [AccountsPermissions.GET_ACCOUNTS_PERMISSION] is assumed to have been granted already in
 * these examples for brevity. All queries will return an empty list if the permission is not granted.
 *
 * ## Usage
 *
 * Here is an example of how to get all accounts and get accounts with type "com.google".
 *
 * ```kotlin
 * val allAccounts : List<Account> = accountsQuery.allAccounts(context)
 * val googleAccounts : List<Account> = accountsQuery.accountsWithType(context, "com.google")
 * ```
 *
 * In Java,
 *
 * ```java
 * List<Account> allAccounts = accountsQuery.allAccounts(context)
 * List<Account> googleAccounts = accountsQuery.accountsWithType(context, "com.google")
 * ```
 *
 * ## Filtering
 *
 * Assuming that there are not that many Accounts per user / device (can you think of someone that
 * has over 100 different Accounts that they are logged in to?). This assumption means that the
 * query function of Accounts need not be as extensive (or at all) as Contacts Query. Filter, order,
 * offset, and limit functions are left to consumers to implement if they wish.
 */
interface AccountsQuery {

    /**
     * Returns all available [Account]s.
     *
     * ## Thread Safety
     *
     * This is safe to call in any thread, including the UI thread.
     */
    fun allAccounts(): List<Account>

    /**
     * Returns all available [Account]s with the given [Account.type].
     *
     * ## Thread Safety
     *
     * This is safe to call in any thread, including the UI thread.
     */
    fun accountsWithType(type: String): List<Account>
}

@Suppress("FunctionName")
internal fun AccountsQuery(context: Context): AccountsQuery = AccountsQueryImpl(
    AccountManager.get(context),
    AccountsPermissions(context)
)

@SuppressWarnings("MissingPermission")
private class AccountsQueryImpl(
    private val accountManager: AccountManager,
    private val permissions: AccountsPermissions
) : AccountsQuery {

    override fun allAccounts(): List<Account> = accounts {
        accountManager.accounts.asList()
    }

    override fun accountsWithType(type: String) = accounts {
        accountManager.getAccountsByType(type).asList()
    }

    private inline fun accounts(accounts: () -> List<Account>): List<Account> {
        if (!permissions.canGetAccounts()) {
            return emptyList()
        }

        return accounts()
    }
}