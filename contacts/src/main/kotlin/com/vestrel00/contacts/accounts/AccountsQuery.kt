package com.vestrel00.contacts.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.entities.cursor.account
import com.vestrel00.contacts.entities.cursor.getNextOrNull
import com.vestrel00.contacts.entities.cursor.rawContactsCursor
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.util.isEmpty
import com.vestrel00.contacts.util.query

/**
 * Retrieves [Account]s from the [AccountManager] or from the Contacts Provider RawContacts table.
 *
 * ## Permissions
 *
 * The [AccountsPermissions.GET_ACCOUNTS_PERMISSION] OR [ContactsPermissions.READ_PERMISSION]
 * (see function documentation) is assumed to have been granted already in these examples for
 * brevity. All queries will return an empty list if the permission is not granted.
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
     * Returns all available [Account]s.
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
     * Returns all available [Account]s with the given [Account.type].
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
     * Returns the [Account] for the given [rawContact]. Returns null if the [rawContact] is a local
     * RawContact, which is not associated with any account.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountFor(rawContact: RawContactEntity): Account?

    /**
     * Returns the [AccountsList] for the given [rawContacts].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION].
     *
     * ## Cancellation
     *
     * The number of contacts and contact data found and processed may be large, which results
     * in this operation to take a while. Therefore, cancellation is supported while the contacts
     * list is being built. To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun accountsFor(vararg rawContacts: RawContactEntity, cancel: () -> Boolean = { false })
    fun accountsFor(vararg rawContacts: RawContactEntity, cancel: () -> Boolean): AccountsList

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(rawContacts: Collection<RawContactEntity>, cancel: () -> Boolean): AccountsList

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(vararg rawContacts: RawContactEntity): AccountsList

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(rawContacts: Sequence<RawContactEntity>): AccountsList

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(rawContacts: Sequence<RawContactEntity>, cancel: () -> Boolean): AccountsList

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(rawContacts: Collection<RawContactEntity>): AccountsList

    /**
     * The list of [Account]s retrieved in the same order as the given list of [RawContactEntity].
     *
     * This list allows for null [Account]s to accommodate for local RawContacts.
     */
    interface AccountsList : List<Account?> {

        /**
         * The [Account] retrieved for the [rawContact]. Null if no Account or retrieval failed.
         */
        fun accountFor(rawContact: RawContactEntity): Account?

        /**
         * The [Account] retrieved for the [RawContactEntity] with [rawContactId]. Null if no
         * Account or retrieval failed.
         */
        fun accountFor(rawContactId: Long): Account?
    }
}

@Suppress("FunctionName")
internal fun AccountsQuery(context: Context): AccountsQuery = AccountsQueryImpl(
    context.contentResolver,
    AccountManager.get(context),
    AccountsPermissions(context)
)

@SuppressWarnings("MissingPermission")
private class AccountsQueryImpl(
    private val contentResolver: ContentResolver,
    private val accountManager: AccountManager,
    private val permissions: AccountsPermissions
) : AccountsQuery {

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

    override fun accountFor(rawContact: RawContactEntity): Account? =
        if (!permissions.canQueryAccounts()) {
            null
        } else {
            rawContact.id?.let(contentResolver::accountForRawContactWithId)
        }

    override fun accountsFor(vararg rawContacts: RawContactEntity, cancel: () -> Boolean) =
        accountsFor(rawContacts.asSequence(), cancel)

    override fun accountsFor(vararg rawContacts: RawContactEntity) =
        accountsFor(rawContacts.asSequence())

    override fun accountsFor(rawContacts: Collection<RawContactEntity>, cancel: () -> Boolean) =
        accountsFor(rawContacts.asSequence(), cancel)

    override fun accountsFor(rawContacts: Collection<RawContactEntity>) =
        accountsFor(rawContacts.asSequence())

    override fun accountsFor(rawContacts: Sequence<RawContactEntity>) =
        accountsFor(rawContacts) { false }

    override fun accountsFor(rawContacts: Sequence<RawContactEntity>, cancel: () -> Boolean):
            AccountsQuery.AccountsList {

        if (!permissions.canQueryAccounts()) {
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
                Table.RAW_CONTACTS,
                Include(RawContactsFields),
                RawContactsFields.Id `in` nonNullRawContactIds
            ) {
                val rawContactsCursor = it.rawContactsCursor()
                while (!cancel() && it.moveToNext()) {
                    val rawContactId = rawContactsCursor.rawContactId
                    if (rawContactId != null) {
                        put(rawContactId, rawContactsCursor.account())
                    }
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
    Table.RAW_CONTACTS,
    Include(RawContactsFields.AccountName, RawContactsFields.AccountType),
    RawContactsFields.Id equalTo rawContactId
) {
    it.getNextOrNull { it.rawContactsCursor().account() }
}

private class AccountsListImpl(private val rawContactIdAccountMap: Map<Long, Account?>) :
    ArrayList<Account?>(), AccountsQuery.AccountsList {

    override fun accountFor(rawContact: RawContactEntity): Account? =
        rawContact.id?.let(::accountFor)

    override fun accountFor(rawContactId: Long): Account? = rawContactIdAccountMap[rawContactId]
}