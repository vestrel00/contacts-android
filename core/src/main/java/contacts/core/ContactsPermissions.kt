package contacts.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Process
import contacts.core.ContactsPermissions.Companion.READ_PERMISSION
import contacts.core.ContactsPermissions.Companion.WRITE_PERMISSION
import contacts.core.accounts.AccountsPermissions.Companion.GET_ACCOUNTS_PERMISSION

/**
 * Provides functions for checking required permissions.
 */
interface ContactsPermissions {

    /**
     * Returns true if [READ_PERMISSION] is granted.
     */
    val canQuery: Boolean

    /**
     * Returns true if [WRITE_PERMISSION] and [GET_ACCOUNTS_PERMISSION] are granted.
     */
    val canInsert: Boolean

    /**
     * Returns true if [WRITE_PERMISSION] is granted.
     */
    val canUpdateDelete: Boolean

    companion object {
        const val READ_PERMISSION: String = Manifest.permission.READ_CONTACTS
        const val WRITE_PERMISSION: String = Manifest.permission.WRITE_CONTACTS
    }
}

@Suppress("FunctionName")
internal fun ContactsPermissions(context: Context): ContactsPermissions =
    ContactsPermissionsImpl(context.applicationContext)

private class ContactsPermissionsImpl(
    private val applicationContext: Context
) : ContactsPermissions {

    override val canQuery: Boolean
        get() = applicationContext.isPermissionGrantedFor(READ_PERMISSION)

    override val canInsert: Boolean
        get() = applicationContext.isPermissionGrantedFor(WRITE_PERMISSION)
                && applicationContext.isPermissionGrantedFor(GET_ACCOUNTS_PERMISSION)

    override val canUpdateDelete: Boolean
        get() = applicationContext.isPermissionGrantedFor(WRITE_PERMISSION)
}

// [ANDROID X] Same as ContextCompat.checkSelfPermission, which we are not using to avoid having
// to include androidx.core as a dependency (just for this one method)!
internal fun Context.isPermissionGrantedFor(permission: String): Boolean =
    checkPermission(permission, Process.myPid(), Process.myUid()) == PERMISSION_GRANTED
