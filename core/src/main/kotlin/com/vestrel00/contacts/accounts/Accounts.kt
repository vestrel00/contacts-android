package com.vestrel00.contacts.accounts

import android.content.Context

/**
 * Provides new [AccountsQuery], [AccountsRawContactsQuery], and
 * [AccountsRawContactsAssociationsUpdate] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.GET_ACCOUNTS" and "android.permission.READ_CONTACTS" to the
 *   AndroidManifest in order to use [query].
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to use
 *   [queryRawContacts].
 * - Add the "android.permission.GET_ACCOUNTS" and "android.permission.WRITE_CONTACTS" to the
 *   AndroidManifest in order to use [updateRawContactsAssociations].
 *
 * Use [permissions] convenience functions to check for required permissions.
 */
interface Accounts {

    /**
     * Returns a new [AccountsQuery] instance.
     */
    fun query(context: Context): AccountsQuery

    /**
     * Returns a new [AccountsRawContactsQuery] instance.
     */
    fun queryRawContacts(context: Context): AccountsRawContactsQuery

    /**
     * Returns a new [AccountsRawContactsAssociationsUpdate] instance.
     */
    fun updateRawContactsAssociations(context: Context): AccountsRawContactsAssociationsUpdate

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

    override fun queryRawContacts(context: Context) = AccountsRawContactsQuery(context)

    override fun updateRawContactsAssociations(context: Context) =
        AccountsRawContactsAssociationsUpdate(context)

    override fun permissions(context: Context) = AccountsPermissions(context)
}