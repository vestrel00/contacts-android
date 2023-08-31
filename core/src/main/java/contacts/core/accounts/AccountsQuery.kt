package contacts.core.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.provider.ContactsContract
import contacts.core.*
import contacts.core.util.*

/**
 * Retrieves **visible** [Account]s from the [AccountManager] whose [Account.type] has a matching
 * [android.content.SyncAdapterType.accountType] where the
 * [android.content.SyncAdapterType.authority] is equal to
 * [android.provider.ContactsContract.AUTHORITY].
 *
 * If you look at all of the Accounts returned by the [AccountManager.getAccounts] in your 3rd
 * party app, you might see the following [Account.type]s...
 *
 * - "com.google" if you are signed into a Google account
 * - "com.google.android.gm.legacyimap" if you are signed into an Personal (IMAP) account
 * - "com.samsung.android.mobileservice" if the device is from Samsung
 *
 * When you open the Google Contacts app (assuming that it is a 3rd party app that did not come
 * pre-installed in the OS) and select an Account to save a new Contact to, you will notice that it
 * only allows you to choose between the Google account of local/device account. It does not show
 * the Personal (IMAP) account or the Samsung Account. The reason is (probably) because there is
 * no sync adapter for Contacts for those accounts. This API filters such accounts for you because
 * this API is specific to accounts relating to Contacts!
 *
 * Another thing to note is that Samsung (type "com.osp.app.signin"), Xiaomi (type "com.xiaomi"),
 * and perhaps other OEMs do not allow 3rd party (non-system) apps (those that do not come
 * pre-installed in the OS) to access their accounts. Your app does not have visibility on this
 * accounts, unless this API is packaged as part of a custom Android OS, which would be super cool.
 * Such accounts are not returned by this query. In a Samsung device, the Samsung Contacts app
 * is able to show the Samsung account but the Google Contacts app cannot.
 *
 * ## Permissions
 *
 * The [AccountsPermissions.GET_ACCOUNTS_PERMISSION] is assumed to have been granted already in
 * these examples for brevity. If not granted, the query will do
 * nothing and return an empty list.
 *
 * ## Usage
 *
 * Here is an example of how to get all accounts with type "com.google".
 *
 * In Kotlin,
 *
 * ```kotlin
 * val accounts = accountsQuery.withType("com.google").find()
 * ```
 *
 * In Java,
 *
 * ```kotlin
 * List<Account> accounts = accountsQuery.withType("com.google").find();
 * ```
 *
 * ## Where, orderBy, offset, and limit
 *
 * Assuming that there are not that many Accounts per user / device (can you think of someone that
 * has over 100 different Accounts that they are logged in to?). This assumption means that the
 * query function of Accounts need not be as extensive (or at all) as other Queries. Where, orderBy,
 * offset, and limit functions are left to consumers to implement if they wish.
 *
 * ## Invalid or invisible Accounts
 *
 * Samsung Accounts (type "com.osp.app.signin") and Xiaomi Accounts (type "com.xiaomi") on Samsung
 * and Xiaomi devices respectively are not returned by the [AccountManager] if the calling app is a
 * 3rd party app (does not come pre-installed with the OS).
 *
 * In Samsung devices, the default Contacts app that comes pre-installed is Samsung Contacts. The
 * Samsung Contacts app allows users to create and filter contacts using the Samsung account.
 * However, 3rd party apps such as Google Contacts do not have visibility of the Samsung account.
 * Contacts belonging to Samsung accounts are still accessible to 3rd party apps, it's just that the
 * Accounts are not.
 *
 * In Xiaomi devices, the default Contacts app that comes pre-installed (in the global version) is
 * the Google Contacts app. The Google Contacts app allows users to create and filter contacts
 * using the Samsung account. However, 3rd party Contacts apps do not have visibility of the Xiaomi
 * account. Contacts belonging to Xiaomi accounts are still accessible to 3rd party apps, it's just
 * that the Accounts are not.
 *
 * There may be other OEMs aside from Samsung and Xiaomi that have similar caveats.
 */
interface AccountsQuery : CrudApi {

    /**
     * Limits the search to Accounts that have one of the given [accountTypes].
     *
     * If this is not specified or none is provided (empty list), then Accounts with any type are
     * included in the search.
     */
    fun withTypes(vararg accountTypes: String): AccountsQuery

    /**
     * See [AccountsQuery.withTypes].
     */
    fun withTypes(accountTypes: Collection<String>): AccountsQuery

    /**
     * See [AccountsQuery.withTypes].
     */
    fun withTypes(accountTypes: Sequence<String>): AccountsQuery

    /**
     * Returns a list of [Accounts]s matching the preceding query options.
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun find(): Result

    /**
     * Returns a list of [Accounts]s matching the preceding query options.
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * ## Cancellation
     *
     * Cancellation is supported while the accounts list is being built. To cancel at any time, the
     * [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **An empty list will be returned if cancelled.**
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun find(cancel: () -> Boolean = { false }): Result
    fun find(cancel: () -> Boolean): Result

    /**
     * Returns a redacted instance where all private user data are redacted.
     *
     * ## Redacted instances may produce invalid results!
     *
     * Redacted instance may have critical information redacted, which is required to make
     * the operation work properly.
     *
     * **Redacted operations should typically only be used for logging in production!**
     */
    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): AccountsQuery

    /**
     * A list of [Account]s.
     */
    interface Result : List<Account>, CrudApi.Result {

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun AccountsQuery(contacts: Contacts): AccountsQuery =
    AccountsQueryImpl(
        contacts,
        AccountManager.get(contacts.applicationContext)
    )

@SuppressWarnings("MissingPermission")
private class AccountsQueryImpl(
    override val contactsApi: Contacts,
    private val accountManager: AccountManager,

    private val accountTypes: MutableSet<String> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : AccountsQuery {

    override fun toString(): String =
        """
            AccountsQuery {
                accountType: $accountTypes
                hasPermission: ${accountsPermissions.canQueryAccounts()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): AccountsQuery = AccountsQueryImpl(
        contactsApi, accountManager,

        accountTypes = accountTypes.redactStrings().toMutableSet(),

        isRedacted = true
    )

    override fun withTypes(vararg accountTypes: String) = withTypes(accountTypes.asSequence())

    override fun withTypes(accountTypes: Collection<String>) = withTypes(accountTypes.asSequence())

    override fun withTypes(accountTypes: Sequence<String>): AccountsQuery = apply {
        this.accountTypes.addAll(accountTypes.redactStringsOrThis(isRedacted))
    }

    override fun find(): AccountsQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): AccountsQuery.Result {
        onPreExecute()

        // We start off with the full set of accounts in the system (which is typically not
        // more than a handful). Then we'll trim the fat as we process the query parameters.
        // This will not include Samsung's or Xiaomi's local phone "account".
        val visibleAccounts: MutableSet<Account> = accountManager.accounts.toMutableSet()

        return if (
            cancel()
            || !accountsPermissions.canQueryAccounts()
            // No (visible) accounts in the system. No point in processing the rest of the query.
            || visibleAccounts.isEmpty()
        ) {
            AccountsQueryResult(visibleAccounts)
        } else {
            if (!cancel() && accountTypes.isNotEmpty()) {
                // Reduce the accounts to only those that have the given types.
                visibleAccounts.removeAll { !accountTypes.contains(it.type) }
            }

            if (!cancel()) {
                // Remove accounts that do not have a sync adapter for contacts.
                val accountTypesWithSyncAdapterForContacts = ContentResolver.getSyncAdapterTypes()
                    .filter { it.authority == ContactsContract.AUTHORITY }
                    .map { it.accountType }
                visibleAccounts.removeAll {
                    !accountTypesWithSyncAdapterForContacts.contains(it.type)
                }
            }

            if (cancel()) {
                visibleAccounts.clear()
            }

            AccountsQueryResult(visibleAccounts)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private class AccountsQueryResult private constructor(
    accounts: Set<Account>,
    override val isRedacted: Boolean
) : ArrayList<Account>(accounts), AccountsQuery.Result {

    constructor(accounts: Set<Account>) : this(accounts, false)

    // Note that it should be okay to print all of the Accounts as there shouldn't be much.
    override fun toString(): String =
        """
            AccountsQuery.Result {
                Number of accounts found: $size
                Accounts: ${joinToString()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): AccountsQuery.Result = AccountsQueryResult(
        asSequence().redactedCopies().toSet(),
        isRedacted = true
    )
}