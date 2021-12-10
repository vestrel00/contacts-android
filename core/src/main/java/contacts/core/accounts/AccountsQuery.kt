package contacts.core.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import contacts.core.Include
import contacts.core.RawContactsFields
import contacts.core.`in`
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.cursor.account
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.equalTo
import contacts.core.util.isEmpty
import contacts.core.util.isProfileId
import contacts.core.util.query

/**
 * Retrieves [Account]s from the [AccountManager] or from the Contacts Provider RawContacts table.
 *
 * ## Permissions
 *
 * The [AccountsPermissions.GET_ACCOUNTS_PERMISSION] OR
 * [contacts.core.ContactsPermissions.READ_PERMISSION] (see function documentation) is assumed to
 * have been granted already in these examples for brevity. If not granted, the query will do
 * nothing and return an empty list.
 *
 * ## Usage
 *
 * Here is an example of how to get all accounts and get accounts with type "com.google".
 *
 * ```kotlin
 * val allAccounts : List<Account> = accountsQuery.allAccounts(context)
 * val googleAccounts : List<Account> = accountsQuery.accountsWithType(context, "com.google")
 * val accountsForRawContacts: Result = accountsQuery.accountsFor(rawContacts)
 * ```
 *
 * ## Where, orderBy, offset, and limit
 *
 * Assuming that there are not that many Accounts per user / device (can you think of someone that
 * has over 100 different Accounts that they are logged in to?). This assumption means that the
 * query function of Accounts need not be as extensive (or at all) as other Queries. Where, orderBy,
 * offset, and limit functions are left to consumers to implement if they wish.
 */
interface AccountsQuery {

    /**
     * Returns all available [Account]s in the system.
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * ## Thread Safety
     *
     * This is safe to call in any thread, including the UI thread.
     */
    fun allAccounts(): List<Account>

    /**
     * Returns all available [Account]s with the given [Account.type] in the system.
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * ## Thread Safety
     *
     * This is safe to call in any thread, including the UI thread.
     */
    fun accountsWithType(type: String): List<Account>

    /**
     * Returns the [Account] for the given Profile or non-Profile (depending on instance)
     * [rawContact]. Returns null if the [rawContact] is a local RawContact, which is not associated
     * with any account.
     *
     * ## Permissions
     *
     * Requires [contacts.core.ContactsPermissions.READ_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountFor(rawContact: ExistingRawContactEntity): Account?

    /**
     * Returns the [AccountsList] for the given Profile or non-Profile (depending on instance)
     * [rawContacts].
     *
     * ## Permissions
     *
     * Requires [contacts.core.ContactsPermissions.READ_PERMISSION].
     *
     * ## Cancellation
     *
     * Cancellation is supported while the query is in progress. To cancel at any time, the
     * [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun accountsFor(vararg rawContacts: ExistingRawContactEntity, cancel: () -> Boolean = { false })
    fun accountsFor(
        vararg rawContacts: ExistingRawContactEntity,
        cancel: () -> Boolean
    ): AccountsList

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(
        rawContacts: Sequence<ExistingRawContactEntity>,
        cancel: () -> Boolean
    ): AccountsList

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(
        rawContacts: Collection<ExistingRawContactEntity>,
        cancel: () -> Boolean
    ): AccountsList

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(vararg rawContacts: ExistingRawContactEntity): AccountsList

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(rawContacts: Sequence<ExistingRawContactEntity>): AccountsList

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(rawContacts: Collection<ExistingRawContactEntity>): AccountsList

    /**
     * The list of [Account]s retrieved in the same order as the given list of [ExistingRawContactEntity].
     *
     * This list allows for null [Account]s to accommodate for local RawContacts.
     */
    interface AccountsList : List<Account?> {

        /**
         * The [Account] retrieved for the [rawContact]. Null if no Account or retrieval failed.
         */
        fun accountFor(rawContact: ExistingRawContactEntity): Account?

        /**
         * The [Account] retrieved for the [ExistingRawContactEntity] with [rawContactId]. Null if no
         * Account or retrieval failed.
         */
        fun accountFor(rawContactId: Long): Account?
    }
}

@Suppress("FunctionName")
internal fun AccountsQuery(accounts: Accounts, isProfile: Boolean): AccountsQuery =
    AccountsQueryImpl(
        accounts.applicationContext.contentResolver,
        AccountManager.get(accounts.applicationContext),
        accounts.permissions,
        isProfile
    )

@SuppressWarnings("MissingPermission")
private class AccountsQueryImpl(
    private val contentResolver: ContentResolver,
    private val accountManager: AccountManager,
    private val permissions: AccountsPermissions,
    private val isProfile: Boolean
) : AccountsQuery {

    override fun toString(): String =
        """
            AccountsQuery {
                isProfile: $isProfile
            }
        """.trimIndent()

    override fun allAccounts(): List<Account> = if (!permissions.canQueryAccounts()) {
        emptyList()
    } else {
        accountManager.accounts.asList()
    }

    override fun accountsWithType(type: String): List<Account> =
        if (!permissions.canQueryAccounts()) {
            emptyList()
        } else {
            accountManager.getAccountsByType(type).asList()
        }

    override fun accountFor(rawContact: ExistingRawContactEntity): Account? =
        if (!permissions.canQueryAccounts() || rawContact.isProfile != isProfile) {
            // Intentionally fail the operation to ensure that this is only used for intended
            // profile or non-profile operations. Otherwise, operation can succeed. This is only
            // done to enforce API design.
            null
        } else {
            contentResolver.accountForRawContactWithId(rawContact.id)
        }

    override fun accountsFor(vararg rawContacts: ExistingRawContactEntity, cancel: () -> Boolean) =
        accountsFor(rawContacts.asSequence(), cancel)

    override fun accountsFor(vararg rawContacts: ExistingRawContactEntity) =
        accountsFor(rawContacts.asSequence())

    override fun accountsFor(
        rawContacts: Collection<ExistingRawContactEntity>,
        cancel: () -> Boolean
    ) =
        accountsFor(rawContacts.asSequence(), cancel)

    override fun accountsFor(rawContacts: Collection<ExistingRawContactEntity>) =
        accountsFor(rawContacts.asSequence())

    override fun accountsFor(rawContacts: Sequence<ExistingRawContactEntity>) =
        accountsFor(rawContacts) { false }

    override fun accountsFor(
        rawContacts: Sequence<ExistingRawContactEntity>,
        cancel: () -> Boolean
    ):
            AccountsQuery.AccountsList {

        if (!permissions.canQueryAccounts()) {
            return AccountsListImpl(emptyMap())
        }

        if (rawContacts.find { it.isProfile != isProfile } != null) {
            // Intentionally fail the operation to ensure that this is only used for intended
            // profile or non-profile operations. Otherwise, operation can succeed. This is only
            // done to enforce API design.
            return AccountsListImpl(emptyMap())
        }

        val rawContactIds = rawContacts.map { it.id }
        val nonNullRawContactIds = rawContactIds.filterNotNull()

        val rawContactIdAccountMap = mutableMapOf<Long, Account?>().apply {
            // Only perform the query if there is at least one nonNullRawContactId
            if (nonNullRawContactIds.isEmpty()) {
                return@apply
            }

            // Get all rows in nonNullRawContactIds.
            contentResolver.query(
                if (isProfile) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
                Include(RawContactsFields),
                RawContactsFields.Id `in` nonNullRawContactIds
            ) {
                val rawContactsCursor = it.rawContactsCursor()
                while (!cancel() && it.moveToNext()) {
                    put(rawContactsCursor.rawContactId, rawContactsCursor.account())
                }
            }

            // Ensure incomplete data sets are not returned.
            if (cancel()) {
                clear()
            }
        }

        return AccountsListImpl(rawContactIdAccountMap).apply {
            // Build the parameter-in-order list with nullable Accounts.
            for (rawContactId in rawContactIds) {
                add(rawContactIdAccountMap[rawContactId])

                if (cancel()) {
                    break
                }
            }

            // Ensure incomplete data sets are not returned.
            if (cancel()) {
                clear()
            }
        }
    }
}

internal fun ContentResolver.accountForRawContactWithId(rawContactId: Long): Account? = query(
    if (rawContactId.isProfileId) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
    Include(RawContactsFields.AccountName, RawContactsFields.AccountType),
    RawContactsFields.Id equalTo rawContactId
) {
    it.getNextOrNull { it.rawContactsCursor().account() }
}

private class AccountsListImpl(private val rawContactIdAccountMap: Map<Long, Account?>) :
    ArrayList<Account?>(), AccountsQuery.AccountsList {

    override fun accountFor(rawContact: ExistingRawContactEntity): Account? =
        accountFor(rawContact.id)

    override fun accountFor(rawContactId: Long): Account? = rawContactIdAccountMap[rawContactId]
}