package com.vestrel00.contacts.accounts

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.`in`
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.entities.cursor.account
import com.vestrel00.contacts.entities.cursor.getNextOrNull
import com.vestrel00.contacts.entities.cursor.rawContactsCursor
import com.vestrel00.contacts.entities.operation.newDelete
import com.vestrel00.contacts.entities.operation.newUpdate
import com.vestrel00.contacts.entities.operation.withSelection
import com.vestrel00.contacts.entities.operation.withValue
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo
import com.vestrel00.contacts.util.applyBatch
import com.vestrel00.contacts.util.nullIfNotInSystem
import com.vestrel00.contacts.util.query

// TODO Update DEV_NOTES data required and groups / group membership sections.
// Contacts Provider automatically creates a group membership to the default group of the target Account when the account changes.
//     - This occurs even if the group membership already exists resulting in duplicates.
// Contacts Provider DOES NOT delete existing group memberships when the account changes.
//     - This has to be done manually to prevent duplicates to the default group.
// For Lollipop (API 22) and below, the Contacts Provider sets null accounts to non-null asynchronously.
//     - Just add a note about this behavior.

/**
 * TODO
 */
interface AccountsRawContactsAssociations {

    // region GET ACCOUNTS

    /**
     * Returns the [Account] for the given [rawContact]. Returns null if the [rawContact] is a local
     * RawContact, which is not associated with any account.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountFor(rawContact: RawContactEntity): Account?

    /**
     * Returns the [AccountsResult] for the given [rawContacts].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(vararg rawContacts: RawContactEntity): AccountsResult

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(rawContacts: Collection<RawContactEntity>): AccountsResult

    /**
     * See [accountsFor].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun accountsFor(rawContacts: Sequence<RawContactEntity>): AccountsResult

    // endregion

    // region ASSOCIATE

    /**
     * Associates the given [rawContacts] with the given [account].
     *
     * RawContacts that were already associated with an Account will no longer be associated with
     * that Account if this call succeeds. Existing group memberships will be deleted. A group
     * membership to the default group of the given [Account] will be created automatically by the
     * Contacts Provider upon successful operation.
     *
     * Only existing RawContacts that have been retrieved via a query will be processed. Those that
     * have been manually created via a constructor will be ignored.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithRawContacts(
        account: Account, vararg rawContacts: RawContactEntity
    ): Boolean

    /**
     * See [associateAccountWithRawContacts].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithRawContacts(
        account: Account, rawContacts: Collection<RawContactEntity>
    ): Boolean

    /**
     * See [associateAccountWithRawContacts].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithRawContacts(
        account: Account, rawContacts: Sequence<RawContactEntity>
    ): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithLocalRawContacts(account: Account): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account, vararg srcAccounts: Account
    ): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account, srcAccounts: Collection<Account>
    ): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account, srcAccounts: Sequence<Account>
    ): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithRawContactsFromAllAccounts(dstAccount: Account): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithAllRawContacts(dstAccount: Account): Boolean

    // endregion

    // region DISSOCIATE

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContacts(vararg rawContacts: RawContactEntity): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContacts(rawContacts: Collection<RawContactEntity>): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContacts(rawContacts: Sequence<RawContactEntity>): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContactsFromAccounts(vararg accounts: Account): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContactsFromAccounts(accounts: Collection<Account>): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContactsFromAccounts(accounts: Sequence<Account>): Boolean

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContactsFromAllAccounts(): Boolean

    // endregion

    interface AccountsResult {

        /**
         * The list of [Account]s retrieved in the same order as the given list of
         * [RawContactEntity].
         */
        val accounts: List<Account?>

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
internal fun AccountsRawContactsAssociations(context: Context): AccountsRawContactsAssociations =
    AccountsRawContactsAssociationsImpl(context)

private class AccountsRawContactsAssociationsImpl(private val context: Context) :
    AccountsRawContactsAssociations {

    // region GET ACCOUNTS

    override fun accountFor(rawContact: RawContactEntity): Account? =
        rawContact.id?.let { accountForRawContactWithId(context, it) }

    override fun accountsFor(vararg rawContacts: RawContactEntity) =
        accountsFor(rawContacts.asSequence())

    override fun accountsFor(rawContacts: Collection<RawContactEntity>) =
        accountsFor(rawContacts.asSequence())

    override fun accountsFor(rawContacts: Sequence<RawContactEntity>):
            AccountsRawContactsAssociations.AccountsResult {

        val rawContactIds = rawContacts.map { it.id }
        val nonNullRawContactIds = rawContactIds.filterNotNull()

        val rawContactIdsResultMap = mutableMapOf<Long, Account?>().apply {
            // Only perform the query if there is at least one nonNullRawContactId
            if (nonNullRawContactIds.count() == 0) {
                return@apply
            }

            // Get all rows in nonNullRawContactIds.
            context.contentResolver.query(
                Table.RAW_CONTACTS,
                Include(Fields.RawContacts),
                Fields.RawContacts.Id `in` nonNullRawContactIds
            ) {
                val rawContactsCursor = it.rawContactsCursor()
                while (it.moveToNext()) {
                    val rawContactId = rawContactsCursor.rawContactId
                    if (rawContactId != null) {
                        put(rawContactId, rawContactsCursor.account())
                    }
                }
            }
        }

        // Build the parameter-in-order list with nullable Accounts.
        val accounts = mutableListOf<Account?>().apply {
            for (rawContactId in rawContactIds) {
                add(rawContactIdsResultMap[rawContactId])
            }
        }

        return AccountsResultImpl(accounts, rawContactIdsResultMap)
    }

    // endregion

    // region ASSOCIATE

    override fun associateAccountWithRawContacts(
        account: Account, vararg rawContacts: RawContactEntity
    ) = associateAccountWithRawContacts(account, rawContacts.asSequence())

    override fun associateAccountWithRawContacts(
        account: Account, rawContacts: Collection<RawContactEntity>
    ) = associateAccountWithRawContacts(account, rawContacts.asSequence())

    override fun associateAccountWithRawContacts(
        account: Account, rawContacts: Sequence<RawContactEntity>
    ): Boolean {

        // Only existing RawContacts can be associated with an Account.
        val nonNullRawContactIds = rawContacts.map { it.id }.filterNotNull()

        if (nonNullRawContactIds.count() == 0) {
            return false
        }

        // A valid account is required for associations.
        account.nullIfNotInSystem(context) ?: return false

        return context.contentResolver.updateRawContactsAccounts(nonNullRawContactIds, account)
    }

    override fun associateAccountWithLocalRawContacts(account: Account): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account,
        vararg srcAccounts: Account
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account,
        srcAccounts: Collection<Account>
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account,
        srcAccounts: Sequence<Account>
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun associateAccountWithRawContactsFromAllAccounts(dstAccount: Account): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun associateAccountWithAllRawContacts(dstAccount: Account): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // endregion

    // region DISSOCIATE

    override fun dissociateRawContacts(vararg rawContacts: RawContactEntity): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dissociateRawContacts(rawContacts: Collection<RawContactEntity>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dissociateRawContacts(rawContacts: Sequence<RawContactEntity>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dissociateRawContactsFromAccounts(vararg accounts: Account): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dissociateRawContactsFromAccounts(accounts: Collection<Account>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dissociateRawContactsFromAccounts(accounts: Sequence<Account>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dissociateRawContactsFromAllAccounts(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // endregion
}

internal fun accountForRawContactWithId(context: Context, rawContactId: Long): Account? =
    context.contentResolver.query(
        Table.RAW_CONTACTS,
        Include(Fields.RawContacts.AccountName, Fields.RawContacts.AccountType),
        Fields.RawContacts.Id equalTo rawContactId
    ) {
        it.getNextOrNull { it.rawContactsCursor().account() }
    }

/**
 * Deletes existing group memberships in the Data table of the given [rawContactIds] and then
 * updates the sync columns in the RawContacts table with the given [account]. These two operations
 * are done in a batch so either both succeed or both fail.
 */
private fun ContentResolver.updateRawContactsAccounts(
    rawContactIds: Sequence<Long>, account: Account
): Boolean = applyBatch(
    // First delete existing group memberships.
    newDelete(Table.DATA)
        .withSelection(
            (Fields.RawContact.Id `in` rawContactIds)
                    and (Fields.MimeType equalTo MimeType.GROUP_MEMBERSHIP)
        )
        .build(),
    // Then update the sync columns.
    newUpdate(Table.RAW_CONTACTS)
        .withSelection(Fields.RawContacts.Id `in` rawContactIds)
        .withValue(Fields.RawContacts.AccountName, account.name)
        .withValue(Fields.RawContacts.AccountType, account.type)
        .build()
) != null

private class AccountsResultImpl(
    override val accounts: List<Account?>,
    private val rawContactIdsResultMap: Map<Long, Account?>
) : AccountsRawContactsAssociations.AccountsResult {

    override fun accountFor(rawContact: RawContactEntity): Account? =
        rawContact.id?.let(::accountFor)

    override fun accountFor(rawContactId: Long): Account? = rawContactIdsResultMap[rawContactId]
}