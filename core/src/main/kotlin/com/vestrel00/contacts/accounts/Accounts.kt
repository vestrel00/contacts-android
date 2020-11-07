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
    fun query(): AccountsQuery

    /**
     * Returns a new [AccountsRawContactsQuery] instance.
     */
    fun queryRawContacts(): AccountsRawContactsQuery

    /**
     * Returns a new [AccountsRawContactsAssociationsUpdate] instance.
     */
    fun updateRawContactsAssociations(): AccountsRawContactsAssociationsUpdate

    /**
     * Returns a new [AccountsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    fun permissions(): AccountsPermissions

    /**
     * Reference to the Application's Context for use in extension functions and external library
     * modules. This is safe to hold on to. Not meant for consumer use.
     */
    val applicationContext: Context
}

/**
 * Creates a new [Accounts] instance.
 */
@Suppress("FunctionName")
fun Accounts(context: Context): Accounts = AccountsImpl(context.applicationContext)

@SuppressWarnings("MissingPermission")
private class AccountsImpl(override val applicationContext: Context) : Accounts {

    override fun query() = AccountsQuery(applicationContext)

    override fun queryRawContacts() = AccountsRawContactsQuery(applicationContext)

    override fun updateRawContactsAssociations() =
        AccountsRawContactsAssociationsUpdate(applicationContext)

    override fun permissions() = AccountsPermissions(applicationContext)
}