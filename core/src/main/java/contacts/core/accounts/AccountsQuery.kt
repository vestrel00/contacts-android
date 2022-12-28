package contacts.core.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.cursor.account
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.util.*

/**
 * Retrieves [Account]s from the [AccountManager].
 *
 * ## Permissions
 *
 * The [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
 * [contacts.core.ContactsPermissions.READ_PERMISSION] are assumed to
 * have been granted already in these examples for brevity. If not granted, the query will do
 * nothing and return an empty list.
 *
 * ## Usage
 *
 * Here is an example of how to get all accounts with type "com.google" that is associated with
 * any of the given rawContacts.
 *
 * In Kotlin,
 *
 * ```kotlin
 * val accounts = accountsQuery
 *     .withType("com.google")
 *     .associatedWith(rawContacts)
 *     .find()
 * ```
 *
 * In Java,
 *
 * ```kotlin
 * List<Account> accounts = accountsQuery
 *     .withType("com.google")
 *     .associatedWith(rawContacts)
 *     .find();
 * ```
 *
 * If [withTypes] and [associatedWith] are not used, then all accounts in the system are returned.
 *
 * ## Where, orderBy, offset, and limit
 *
 * Assuming that there are not that many Accounts per user / device (can you think of someone that
 * has over 100 different Accounts that they are logged in to?). This assumption means that the
 * query function of Accounts need not be as extensive (or at all) as other Queries. Where, orderBy,
 * offset, and limit functions are left to consumers to implement if they wish.
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
     * Limits the search to Accounts that are associated with one of the given [rawContacts]s.
     *
     * If this is not specified or none is provided (empty list), then Accounts associated with any
     * RawContact (or no RawContact) are included in the search.
     *
     * Account info for RawContacts provided here can be retrieved via [Result.accountFor].
     *
     * ## Performance
     *
     * This will require an additional database query, internally performed in this function, which
     * increases the time it takes for [find] to complete. Therefore, you should only specify this
     * if you actually need it.
     */
    fun associatedWith(vararg rawContacts: ExistingRawContactEntity): AccountsQuery

    /**
     * See [AccountsQuery.associatedWith].
     */
    fun associatedWith(rawContacts: Collection<ExistingRawContactEntity>): AccountsQuery

    /**
     * See [AccountsQuery.associatedWith].
     */
    fun associatedWith(rawContacts: Sequence<ExistingRawContactEntity>): AccountsQuery

    /**
     * Limits the search to Accounts that are associated with one of the RawContacts with the
     * given [rawContactIds].
     *
     * If this is not specified or none is provided (empty list), then Accounts associated with any
     * RawContact (or no RawContact) are included in the search.
     *
     * Account info for RawContact IDs provided here can be retrieved via [Result.accountFor].
     *
     * ## Performance
     *
     * This will require an additional database query, internally performed in this function, which
     * increases the time it takes for [find] to complete. Therefore, you should only specify this
     * if you actually need it.
     */
    fun associatedWithRawContactIds(vararg rawContactIds: Long): AccountsQuery

    /**
     * See [AccountsQuery.associatedWithRawContactIds].
     */
    fun associatedWithRawContactIds(rawContactIds: Collection<Long>): AccountsQuery

    /**
     * See [AccountsQuery.associatedWithRawContactIds].
     */
    fun associatedWithRawContactIds(rawContactIds: Sequence<Long>): AccountsQuery

    /**
     * Returns a list of [Accounts]s matching the preceding query options.
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
     * [contacts.core.ContactsPermissions.READ_PERMISSION].
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
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
     * [contacts.core.ContactsPermissions.READ_PERMISSION].
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
     *
     * Use [accountFor] to retrieve the Account for the specified RawContact.
     */
    interface Result : List<Account>, CrudApi.Result {

        /**
         * The [Account] retrieved for the [rawContact]. Null if no Account or retrieval failed.
         *
         * ## Use only when [associatedWith] is the only query parameter used!
         *
         * This will only work for RawContacts that have been specified  in [associatedWith]. If
         * you did not specify RawContacts in [associatedWith], then this is guaranteed to return
         * null even if the [rawContact] actually has an account in the database.
         *
         * Using [withTypes] may also interfere with the value returned here. If the RawContact
         * specified in [associatedWith] does not have an Account with a type specified in
         * [withTypes], then this will return null even if the [rawContact] actually has an account
         * in the database.
         */
        fun accountFor(rawContact: ExistingRawContactEntity): Account?

        /**
         * See [Result.accountFor]
         */
        fun accountFor(rawContactId: Long): Account?

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun AccountsQuery(contacts: Contacts, isProfile: Boolean): AccountsQuery =
    AccountsQueryImpl(
        contacts,
        AccountManager.get(contacts.applicationContext),
        isProfile
    )

@SuppressWarnings("MissingPermission")
private class AccountsQueryImpl(
    override val contactsApi: Contacts,
    private val accountManager: AccountManager,
    private val isProfile: Boolean,

    private val accountTypes: MutableSet<String> = mutableSetOf(),
    private val rawContactIds: MutableSet<Long> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : AccountsQuery {

    override fun toString(): String =
        """
            AccountsQuery {
                isProfile: $isProfile
                accountType: $accountTypes
                rawContactIds: $rawContactIds
                hasPermission: ${accountsPermissions.canQueryAccounts()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): AccountsQuery = AccountsQueryImpl(
        contactsApi, accountManager, isProfile,

        accountTypes = accountTypes.redactStrings().toMutableSet(),
        rawContactIds = rawContactIds,

        isRedacted = true
    )

    override fun withTypes(vararg accountTypes: String) = withTypes(accountTypes.asSequence())

    override fun withTypes(accountTypes: Collection<String>) = withTypes(accountTypes.asSequence())

    override fun withTypes(accountTypes: Sequence<String>): AccountsQuery = apply {
        this.accountTypes.addAll(accountTypes.redactStringsOrThis(isRedacted))
    }

    override fun associatedWith(vararg rawContacts: ExistingRawContactEntity) =
        associatedWith(rawContacts.asSequence())

    override fun associatedWith(rawContacts: Collection<ExistingRawContactEntity>) =
        associatedWith(rawContacts.asSequence())

    override fun associatedWith(rawContacts: Sequence<ExistingRawContactEntity>) =
        associatedWithRawContactIds(rawContacts.map { it.id })

    override fun associatedWithRawContactIds(vararg rawContactIds: Long) =
        associatedWithRawContactIds(rawContactIds.asSequence())

    override fun associatedWithRawContactIds(rawContactIds: Collection<Long>) =
        associatedWithRawContactIds(rawContactIds.asSequence())

    override fun associatedWithRawContactIds(rawContactIds: Sequence<Long>): AccountsQuery = apply {
        this.rawContactIds.addAll(rawContactIds)
    }

    override fun find(): AccountsQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): AccountsQuery.Result {
        onPreExecute()

        // We start off with the full set of accounts in the system (which is typically not
        // more than a handful). Then we'll trim the fat as we process the query parameters.
        var accounts: Set<Account> = accountManager.accounts.toSet()
        return if (
            cancel()
            || !accountsPermissions.canQueryAccounts()
            // If the isProfile parameter does not match for all RawContacts, fail immediately.
            || rawContactIds.allAreProfileIds != isProfile
            // No accounts in the system. No point in processing the rest of the query.
            || accounts.isEmpty()
        ) {
            AccountsQueryResult(accounts, emptyMap())
        } else {
            var rawContactIdsAccountsMap = emptyMap<Long, Account>()

            if (!cancel() && accountTypes.isNotEmpty()) {
                // Reduce the accounts to only those that have the given types.
                accounts = accounts.filter {
                    accountTypes.contains(it.type)
                }.toSet()
            }

            if (!cancel() && rawContactIds.isNotEmpty() && accounts.isNotEmpty()) {
                // Reduce the accounts to only those that are associated with one of the
                // RawContacts. Note that this map can only be as large as the amount of
                // rawContactIds passed to it. Therefore, there should not be any need to paginate,
                // unless the consumer passed in way too many RawContact IDs =P
                rawContactIdsAccountsMap = contentResolver.accountsForRawContactsWithIdsInAccounts(
                    rawContactIds, accounts, cancel
                )
                accounts = rawContactIdsAccountsMap.values.toSet()
            }

            if (cancel()) {
                accounts = emptySet()
                rawContactIdsAccountsMap = emptyMap()
            }

            AccountsQueryResult(accounts, rawContactIdsAccountsMap)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

/**
 * Returns a map of RawContact IDs to the corresponding Account for all RawContacts with IDs in
 * [rawContactIds] that belong to one of the Accounts in [accounts].
 *
 * If the [accounts] is empty, it will be ignored. Otherwise, the query will only return RawContacts
 * that are associated with one of the given accounts.
 *
 * This only requires [contacts.core.ContactsPermissions.READ_PERMISSION].
 */
private fun ContentResolver.accountsForRawContactsWithIdsInAccounts(
    rawContactIds: Set<Long>,
    accounts: Set<Account>,
    cancel: () -> Boolean
): Map<Long, Account> = query(
    if (rawContactIds.allAreProfileIds) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
    Include(RawContactsFields.Id, RawContactsFields.AccountName, RawContactsFields.AccountType),
    // Note that if accounts is empty, then accounts.toRawContactsWhere() will return null.
    // Then the following WHERE will resolve to just (RawContactsFields.Id `in` rawContactIds).
    (RawContactsFields.Id `in` rawContactIds) and accounts.toRawContactsWhere()
) {
    val rawContactIdsAccountsMap = mutableMapOf<Long, Account>()
    val rawContactsCursor = it.rawContactsCursor()
    while (!cancel() && it.moveToNext()) {
        // Reuse references to the given set of accounts to reduce memory consumption by avoiding
        // creating duplicate Accounts.
        val account = accounts.find { account ->
            account.name == rawContactsCursor.accountName &&
                    account.type == rawContactsCursor.accountType
        }
        if (account != null) {
            rawContactIdsAccountsMap[rawContactsCursor.rawContactId] = account
        }
    }
    rawContactIdsAccountsMap
} ?: emptyMap()

/**
 * Returns the Account, based on the values in the RawContacts table, for the RawContact with the
 * given [rawContactId].
 *
 * This only requires [contacts.core.ContactsPermissions.READ_PERMISSION].
 */
internal fun ContentResolver.accountForRawContactWithId(rawContactId: Long): Account? = query(
    if (rawContactId.isProfileId) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
    Include(RawContactsFields.AccountName, RawContactsFields.AccountType),
    RawContactsFields.Id equalTo rawContactId
) {
    it.getNextOrNull { it.rawContactsCursor().account() }
}

private class AccountsQueryResult private constructor(
    accounts: Set<Account>,
    private val rawContactIdsAccountsMap: Map<Long, Account>,
    override val isRedacted: Boolean
) : ArrayList<Account>(accounts), AccountsQuery.Result {

    constructor(
        accounts: Set<Account>,
        rawContactIdAccountMap: Map<Long, Account>
    ) : this(accounts, rawContactIdAccountMap, false)

    // Note that it should be okay to print all of the Accounts as there shouldn't be much.
    override fun toString(): String =
        """
            AccountsQuery.Result {
                Number of accounts found: $size
                Accounts: ${joinToString()}
                rawContactIdsAccountsMap: $rawContactIdsAccountsMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): AccountsQuery.Result = AccountsQueryResult(
        asSequence().redactedCopies().toSet(),
        rawContactIdsAccountsMap.mapValues { it.value.redactedCopy() },
        isRedacted = true
    )

    override fun accountFor(rawContact: ExistingRawContactEntity): Account? =
        accountFor(rawContact.id)

    override fun accountFor(rawContactId: Long): Account? = rawContactIdsAccountsMap[rawContactId]
}