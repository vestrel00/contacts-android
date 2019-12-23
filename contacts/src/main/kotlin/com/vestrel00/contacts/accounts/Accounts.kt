package com.vestrel00.contacts.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

/**
 * Provides access to [Account]s.
 *
 * ## Usage
 *
 * The [AccountsPermissions.GET_ACCOUNTS_PERMISSION] is assumed to have been granted already in
 * these examples for brevity. Empty list will be returned if permission is not granted.
 *
 * Here is an example of how to get all accounts and get accounts with type "com.google".
 *
 * ```kotlin
 * val accounts = Accounts()
 * val allAccounts : List<Account> = accounts.allAccounts(context)
 * val googleAccounts : List<Account> = accounts.accountsWithType(context, "com.google")
 * ```
 *
 * In Java,
 *
 * ```java
 * import static com.vestrel00.contacts.accounts.AccountsKt.Accounts;
 *
 * Accounts accounts = Accounts()
 * List<Account> allAccounts = accounts.allAccounts(context)
 * List<Account> googleAccounts = accounts.accountsWithType(context, "com.google")
 * ```
 *
 * Use [permissions] to check for required permission.
 *
 * ## Thread Safety
 *
 * All functions here are safe to call in the Main / UI thread.
 */
interface Accounts {

    /**
     * Returns all available [Account]s.
     *
     * Returns an empty list if the [AccountsPermissions.GET_ACCOUNTS_PERMISSION] is not granted.
     */
    fun allAccounts(context: Context): List<Account>

    /**
     * Returns all available [Account]s with the given [Account.type].
     *
     * Returns an empty list if the [AccountsPermissions.GET_ACCOUNTS_PERMISSION] is not granted.
     */
    fun accountsWithType(context: Context, type: String): List<Account>

    /**
     * Returns a new [AccountsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    fun permissions(context: Context): AccountsPermissions
}

/**
 * Creates a new [Accounts] instance.
 */
@Suppress("FunctionName")
fun Accounts(): Accounts = AccountsImpl()

@SuppressWarnings("MissingPermission")
private class AccountsImpl : Accounts {

    override fun allAccounts(context: Context): List<Account> = accounts(context) {
        AccountManager.get(context).accounts.toList()
    }

    override fun accountsWithType(context: Context, type: String) = accounts(context) {
        AccountManager.get(context).getAccountsByType(type).toList()
    }

    override fun permissions(context: Context) = AccountsPermissions(context)

    private inline fun accounts(context: Context, accounts: () -> List<Account>): List<Account> {
        if (!permissions(context).canGetAccounts()) {
            return emptyList()
        }

        return accounts()
    }
}