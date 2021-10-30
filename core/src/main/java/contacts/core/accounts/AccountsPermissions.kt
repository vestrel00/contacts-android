package contacts.core.accounts

import android.Manifest
import android.content.Context
import contacts.core.ContactsPermissions.Companion.READ_PERMISSION
import contacts.core.ContactsPermissions.Companion.WRITE_PERMISSION
import contacts.core.accounts.AccountsPermissions.Companion.GET_ACCOUNTS_PERMISSION
import contacts.core.isPermissionGrantedFor

/**
 * Provides functions for checking permissions.
 */
interface AccountsPermissions {

    /**
     * Returns true if [GET_ACCOUNTS_PERMISSION] and [READ_PERMISSION] are granted.
     */
    val canQueryAccounts: Boolean

    /**
     * Returns true if [READ_PERMISSION] is granted.
     */
    val canQueryRawContacts: Boolean

    /**
     * Returns true if [GET_ACCOUNTS_PERMISSION] and [WRITE_PERMISSION] are granted.
     */
    val canUpdateRawContactsAssociations: Boolean

    companion object {
        const val GET_ACCOUNTS_PERMISSION: String = Manifest.permission.GET_ACCOUNTS
    }
}

@Suppress("FunctionName")
internal fun AccountsPermissions(context: Context): AccountsPermissions =
    AccountsPermissionsImpl(context.applicationContext)

private class AccountsPermissionsImpl(private val applicationContext: Context) :
    AccountsPermissions {

    override val canQueryAccounts: Boolean
        get() = applicationContext.isPermissionGrantedFor(GET_ACCOUNTS_PERMISSION)
                && applicationContext.isPermissionGrantedFor(READ_PERMISSION)

    override val canQueryRawContacts: Boolean
        get() = applicationContext.isPermissionGrantedFor(READ_PERMISSION)

    override val canUpdateRawContactsAssociations: Boolean
        get() = applicationContext.isPermissionGrantedFor(GET_ACCOUNTS_PERMISSION)
                && applicationContext.isPermissionGrantedFor(WRITE_PERMISSION)
}
