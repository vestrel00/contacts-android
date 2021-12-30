package contacts.core.accounts

import android.content.Context
import contacts.core.Contacts

/**
 * Provides new [AccountsQuery], [AccountsRawContactsQuery], and
 * [AccountsLocalRawContactsUpdate] for Profile OR non-Profile (depending on instance)
 * operations.
 *
 * ## Permissions
 *
 * - Add the "android.permission.GET_ACCOUNTS" and "android.permission.READ_CONTACTS" to the
 *   AndroidManifest in order to use [query].
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to use
 *   [queryRawContacts].
 * - Add the "android.permission.GET_ACCOUNTS" and "android.permission.WRITE_CONTACTS" to the
 *   AndroidManifest in order to use [updateLocalRawContactsAccount].
 *
 * Use [permissions] convenience functions to check for required permissions.
 */
interface Accounts {

    /**
     * Returns a new [AccountsQuery] instance for Profile OR non-Profile (depending on instance)
     * queries.
     */
    fun query(): AccountsQuery

    /**
     * Returns a new [AccountsRawContactsQuery] instance for Profile OR non-Profile (depending on
     * instance) queries.
     */
    fun queryRawContacts(): AccountsRawContactsQuery

    /**
     * Returns a new [AccountsLocalRawContactsUpdate] instance Profile OR non-Profile
     * (depending on instance) RawContacts associations operations.
     */
    fun updateLocalRawContactsAccount(): AccountsLocalRawContactsUpdate

    /**
     * Returns a new [Accounts] instance for Profile operations.
     */
    fun profile(): Accounts

    /**
     * Returns a [AccountsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    val permissions: AccountsPermissions

    /**
     * Reference to the Application's Context for use in extension functions and external library
     * modules. This is safe to hold on to. Not meant for consumer use.
     */
    val applicationContext: Context
}

/**
 * Creates a new [Accounts] instance for Profile or non-Profile operations.
 */
@Suppress("FunctionName")
internal fun Accounts(contacts: Contacts, isProfile: Boolean): Accounts = AccountsImpl(
    contacts.applicationContext,
    AccountsPermissions(contacts.applicationContext),
    isProfile
)

@SuppressWarnings("MissingPermission")
private class AccountsImpl(
    override val applicationContext: Context,
    override val permissions: AccountsPermissions,
    private val isProfile: Boolean
) : Accounts {

    override fun query() = AccountsQuery(this, isProfile)

    override fun queryRawContacts() = AccountsRawContactsQuery(this, isProfile)

    override fun updateLocalRawContactsAccount() =
        AccountsLocalRawContactsUpdate(this, isProfile)

    override fun profile(): Accounts = AccountsImpl(applicationContext, permissions, true)
}