package com.vestrel00.contacts.accounts

import android.Manifest
import android.content.Context
import com.vestrel00.contacts.ContactsPermissions.Companion.READ_PERMISSION
import com.vestrel00.contacts.ContactsPermissions.Companion.WRITE_PERMISSION
import com.vestrel00.contacts.accounts.AccountsPermissions.Companion.GET_ACCOUNTS_PERMISSION
import com.vestrel00.contacts.isPermissionGrantedFor

/**
 * Provides functions for checking permissions.
 */
interface AccountsPermissions {

    /**
     * Returns true if [GET_ACCOUNTS_PERMISSION] and [READ_PERMISSION] are granted.
     */
    fun canQueryAccounts(): Boolean

    /**
     * Returns true if [READ_PERMISSION] is granted.
     */
    fun canQueryRawContacts(): Boolean

    /**
     * Returns true if [GET_ACCOUNTS_PERMISSION] and [WRITE_PERMISSION] are granted.
     */
    fun canUpdateRawContactsAssociations(): Boolean

    companion object {
        const val GET_ACCOUNTS_PERMISSION: String = Manifest.permission.GET_ACCOUNTS
    }
}

@Suppress("FunctionName")
internal fun AccountsPermissions(context: Context): AccountsPermissions =
    AccountsPermissionsImpl(context)

private class AccountsPermissionsImpl(private val context: Context) : AccountsPermissions {

    override fun canQueryAccounts(): Boolean =
        context.isPermissionGrantedFor(GET_ACCOUNTS_PERMISSION)
                && context.isPermissionGrantedFor(READ_PERMISSION)

    override fun canQueryRawContacts(): Boolean = context.isPermissionGrantedFor(READ_PERMISSION)

    override fun canUpdateRawContactsAssociations(): Boolean =
        context.isPermissionGrantedFor(GET_ACCOUNTS_PERMISSION)
                && context.isPermissionGrantedFor(WRITE_PERMISSION)
}
