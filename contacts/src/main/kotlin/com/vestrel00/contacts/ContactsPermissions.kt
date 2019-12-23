package com.vestrel00.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Process
import com.vestrel00.contacts.ContactsPermissions.Companion.READ_PERMISSION
import com.vestrel00.contacts.ContactsPermissions.Companion.WRITE_PERMISSION
import com.vestrel00.contacts.accounts.AccountsPermissions

@Suppress("FunctionName")
internal fun ContactsPermissions(context: Context): ContactsPermissions =
    ContactsPermissionsImpl(context, AccountsPermissions(context))

/**
 * Provides functions for checking required permissions.
 */
interface ContactsPermissions {

    /**
     * Returns true if [READ_PERMISSION] is granted.
     */
    fun canQuery(): Boolean

    /**
     * Returns true if [WRITE_PERMISSION] and [AccountsPermissions.GET_ACCOUNTS_PERMISSION] are
     * granted.
     */
    fun canInsertUpdateDelete(): Boolean

    companion object {
        const val READ_PERMISSION: String = Manifest.permission.READ_CONTACTS
        const val WRITE_PERMISSION: String = Manifest.permission.WRITE_CONTACTS
    }
}

private class ContactsPermissionsImpl(
    private val context: Context,
    private val accountsPermissions: AccountsPermissions
) : ContactsPermissions {

    override fun canQuery() = context.isPermissionGrantedFor(READ_PERMISSION)

    override fun canInsertUpdateDelete() =
        context.isPermissionGrantedFor(WRITE_PERMISSION) && accountsPermissions.canGetAccounts()
}

// [ANDROID X] Same as ContextCompat.checkSelfPermission, which we are not using to avoid having
// to include androidx.core as a dependency (just for this one method)!
internal fun Context.isPermissionGrantedFor(permission: String): Boolean =
    checkPermission(permission, Process.myPid(), Process.myUid()) == PERMISSION_GRANTED
