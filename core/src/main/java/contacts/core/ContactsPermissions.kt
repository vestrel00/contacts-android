package contacts.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Process
import contacts.core.ContactsPermissions.Companion.READ_PERMISSION
import contacts.core.ContactsPermissions.Companion.WRITE_PERMISSION
import contacts.core.accounts.AccountsPermissions.Companion.GET_ACCOUNTS_PERMISSION

/**
 * Provides functions for checking contacts permissions.
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
    fun canInsertToSim(): Boolean

    /**
     * Returns true if [WRITE_PERMISSION] is granted.
     */
    fun canUpdateDelete(): Boolean

    companion object {
        const val READ_PERMISSION: String = Manifest.permission.READ_CONTACTS
        const val WRITE_PERMISSION: String = Manifest.permission.WRITE_CONTACTS
    }
}

internal fun ContactsPermissions(context: Context): ContactsPermissions =
    ContactsPermissionsImpl(context.applicationContext)

private class ContactsPermissionsImpl(
    private val applicationContext: Context
) : ContactsPermissions {

    override fun canQuery(): Boolean = applicationContext.isPermissionGrantedFor(READ_PERMISSION)

    override fun canInsert(): Boolean =
        applicationContext.isPermissionGrantedFor(WRITE_PERMISSION)
                && applicationContext.isPermissionGrantedFor(GET_ACCOUNTS_PERMISSION)

    override fun canInsertToSim(): Boolean =
        applicationContext.isPermissionGrantedFor(WRITE_PERMISSION)

    override fun canUpdateDelete(): Boolean =
        applicationContext.isPermissionGrantedFor(WRITE_PERMISSION)
}

// [ANDROID X] Same as ContextCompat.checkSelfPermission, which we are not using to avoid having
// to include androidx.core as a dependency (just for this one method)!
internal fun Context.isPermissionGrantedFor(permission: String): Boolean =
    checkPermission(permission, Process.myPid(), Process.myUid()) == PERMISSION_GRANTED
