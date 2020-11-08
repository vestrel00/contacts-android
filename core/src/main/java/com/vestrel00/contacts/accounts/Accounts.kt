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
 *   AndroidManifest in order to use [updateRawContactsAssociations] and
 *   [updateProfileRawContactsAssociations].
 *
 * Use [permissions] convenience functions to check for required permissions.
 */
interface Accounts {

    /**
     * Returns a new [AccountsQuery] instance for non-Profile queries.
     */
    fun query(): AccountsQuery

    /**
     * Returns a new [AccountsQuery] instance for Profile queries.
     */
    fun queryProfile(): AccountsQuery

    /**
     * Returns a new [AccountsRawContactsQuery] instance.
     */
    fun queryRawContacts(): AccountsRawContactsQuery

    /**
     * Returns a new [AccountsRawContactsAssociationsUpdate] instance for non-Profile RawContacts.
     *
     * Operations for Profile RawContacts may fail.
     */
    fun updateRawContactsAssociations(): AccountsRawContactsAssociationsUpdate

    /**
     * Returns a new [AccountsRawContactsAssociationsUpdate] instance for Profile RawContacts.
     *
     * Operations for non-Profile RawContacts may fail.
     */
    fun updateProfileRawContactsAssociations(): AccountsRawContactsAssociationsUpdate

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

    override fun query() = AccountsQuery(applicationContext, false)

    override fun queryProfile() = AccountsQuery(applicationContext, true)

    override fun queryRawContacts() = AccountsRawContactsQuery(applicationContext)

    override fun updateRawContactsAssociations() =
        AccountsRawContactsAssociationsUpdate(applicationContext, false)

    override fun updateProfileRawContactsAssociations() =
        AccountsRawContactsAssociationsUpdate(applicationContext, true)

    override fun permissions() = AccountsPermissions(applicationContext)
}