package contacts.core.accounts

import contacts.core.Contacts

/**
 * Provides new [AccountsQuery] and [MoveRawContactsAcrossAccounts].
 *
 * ## Permissions
 *
 * - Add the "android.permission.GET_ACCOUNTS" and "android.permission.READ_CONTACTS" to the
 *   AndroidManifest in order to use [query].
 * - Add the "android.permission.GET_ACCOUNTS", "android.permission.READ_CONTACTS", and
 *   "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to use [move].
 */
interface Accounts {

    /**
     * Returns a new [AccountsQuery] instance for Profile OR non-Profile (depending on instance)
     * queries.
     */
    fun query(): AccountsQuery

    /**
     * Returns a new [MoveRawContactsAcrossAccounts] instance for non-Profile operations.
     */
    fun move(): MoveRawContactsAcrossAccounts

    /**
     * Returns a new [Accounts] instance for Profile operations.
     *
     * Note that the [MoveRawContactsAcrossAccounts] API does NOT support Profile operations.
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
internal fun Accounts(contacts: Contacts, isProfile: Boolean): Accounts =
    AccountsImpl(contacts, isProfile)

private class AccountsImpl(
    override val contactsApi: Contacts,
    private val isProfile: Boolean
) : Accounts {

    override fun query() = AccountsQuery(contactsApi, isProfile)

    override fun move() = MoveRawContactsAcrossAccounts(contactsApi)

    override fun profile(): Accounts = AccountsImpl(contactsApi, true)
}