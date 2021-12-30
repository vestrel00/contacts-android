package contacts.core.accounts

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
     * A reference to the [Contacts] instance that constructed this. This is mostly used internally
     * to shorten internal code.
     *
     * Don't worry, [Contacts] does not keep references to instances of this. There are no circular
     * references that could cause leaks =). [Contacts] is just a factory.
     */
    val contactsApi: Contacts
}

/**
 * Creates a new [Accounts] instance for Profile or non-Profile operations.
 */
@Suppress("FunctionName")
internal fun Accounts(contacts: Contacts, isProfile: Boolean): Accounts = AccountsImpl(
    contacts,
    isProfile
)

@SuppressWarnings("MissingPermission")
private class AccountsImpl(
    override val contactsApi: Contacts,
    private val isProfile: Boolean
) : Accounts {

    override fun query() = AccountsQuery(contactsApi, isProfile)

    override fun queryRawContacts() = AccountsRawContactsQuery(contactsApi, isProfile)

    override fun updateLocalRawContactsAccount() =
        AccountsLocalRawContactsUpdate(contactsApi, isProfile)

    override fun profile(): Accounts = AccountsImpl(contactsApi, true)
}