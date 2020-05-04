package com.vestrel00.contacts.accounts

import android.content.Context

/**
 * Provides new [AccountsQuery] and [AccountsRawContactsAssociations] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.GET_ACCOUNTS" to the AndroidManifest in order to use [query].
 * - Add the "android.permission.GET_ACCOUNTS", "android.permission.READ_CONTACTS", and
 *   "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to use
 *   [rawContactsAssociations].
 *
 * Use [permissions] convenience functions to check for required permissions.
 */
interface Accounts {

    /**
     * Returns a new [AccountsQuery] instance.
     */
    fun query(context: Context): AccountsQuery

    /**
     * Returns a new [AccountsRawContactsAssociations] instance.
     */
    fun rawContactsAssociations(context: Context): AccountsRawContactsAssociations

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

    override fun query(context: Context) = AccountsQuery(context)

    override fun rawContactsAssociations(context: Context) =
        AccountsRawContactsAssociations(context)

    override fun permissions(context: Context) = AccountsPermissions(context)
}