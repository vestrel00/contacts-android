package contacts.core.accounts

import android.Manifest
import android.content.Context
import contacts.core.ContactsPermissions.Companion.READ_PERMISSION
import contacts.core.ContactsPermissions.Companion.WRITE_PERMISSION
import contacts.core.accounts.AccountsPermissions.Companion.GET_ACCOUNTS_PERMISSION
import contacts.core.isPermissionGrantedFor

/**
 * Provides functions for checking account permissions.
 */
interface AccountsPermissions {

    /**
     * Returns true if [GET_ACCOUNTS_PERMISSION] and [READ_PERMISSION] are granted.
     */
    fun canQueryAccounts(): Boolean

    /**
     * Returns true if [GET_ACCOUNTS_PERMISSION], [READ_PERMISSION], and [WRITE_PERMISSION] are
     * granted.
     */
    fun canMoveRawContactsAcrossAccounts(): Boolean

    companion object {
        const val GET_ACCOUNTS_PERMISSION: String = Manifest.permission.GET_ACCOUNTS
    }
}

@Suppress("FunctionName")
internal fun AccountsPermissions(context: Context): AccountsPermissions =
    AccountsPermissionsImpl(context.applicationContext)

private class AccountsPermissionsImpl(private val applicationContext: Context) :
    AccountsPermissions {

    override fun canQueryAccounts(): Boolean =
        applicationContext.isPermissionGrantedFor(GET_ACCOUNTS_PERMISSION)
                && applicationContext.isPermissionGrantedFor(READ_PERMISSION)

    override fun canMoveRawContactsAcrossAccounts(): Boolean =
        applicationContext.isPermissionGrantedFor(GET_ACCOUNTS_PERMISSION)
                && applicationContext.isPermissionGrantedFor(READ_PERMISSION)
                && applicationContext.isPermissionGrantedFor(WRITE_PERMISSION)
}
