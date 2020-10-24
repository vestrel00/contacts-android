package com.vestrel00.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Process
import com.vestrel00.contacts.ContactsPermissions.Companion.READ_PERMISSION
import com.vestrel00.contacts.ContactsPermissions.Companion.WRITE_PERMISSION
import com.vestrel00.contacts.accounts.AccountsPermissions.Companion.GET_ACCOUNTS_PERMISSION

/**
 * Provides functions for checking required permissions.
 */
interface ContactsPermissions {

    /**
     * Returns true if [READ_PERMISSION] is granted.
     */
    fun canQuery(): Boolean

    /**
     * Returns true if [WRITE_PERMISSION] and [GET_ACCOUNTS_PERMISSION] are granted.
     */
    fun canInsert(): Boolean

    /**
     * Returns true if [WRITE_PERMISSION] is granted.
     */
    fun canUpdateDelete(): Boolean

    companion object {
        const val READ_PERMISSION: String = Manifest.permission.READ_CONTACTS
        const val WRITE_PERMISSION: String = Manifest.permission.WRITE_CONTACTS
    }
}

@Suppress("FunctionName")
internal fun ContactsPermissions(context: Context): ContactsPermissions =
    ContactsPermissionsImpl(context)

private class ContactsPermissionsImpl(
    private val context: Context
) : ContactsPermissions {

    override fun canQuery(): Boolean = context.isPermissionGrantedFor(READ_PERMISSION)

    override fun canInsert(): Boolean = context.isPermissionGrantedFor(WRITE_PERMISSION)
            && context.isPermissionGrantedFor(GET_ACCOUNTS_PERMISSION)

    override fun canUpdateDelete(): Boolean = context.isPermissionGrantedFor(WRITE_PERMISSION)
}

// [ANDROID X] Same as ContextCompat.checkSelfPermission, which we are not using to avoid having
// to include androidx.core as a dependency (just for this one method)!
internal fun Context.isPermissionGrantedFor(permission: String): Boolean =
    checkPermission(permission, Process.myPid(), Process.myUid()) == PERMISSION_GRANTED
