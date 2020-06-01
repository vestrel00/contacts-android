package com.vestrel00.contacts.accounts

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.entities.cursor.rawContactsCursor
import com.vestrel00.contacts.entities.operation.newDelete
import com.vestrel00.contacts.entities.operation.newUpdate
import com.vestrel00.contacts.entities.operation.withSelection
import com.vestrel00.contacts.entities.operation.withValue
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.util.*

// TODO Update DEV_NOTES data required and groups / group membership sections.
// Contacts Provider automatically creates a group membership to the default group of the target Account when the account changes.
//     - This occurs even if the group membership already exists resulting in duplicates.
// Contacts Provider DOES NOT delete existing group memberships when the account changes.
//     - This has to be done manually to prevent duplicates to the default group.
// For Lollipop (API 22) and below, the Contacts Provider sets null accounts to non-null asynchronously.
//     - Just add a note about this behavior.

/**
 * TODO Documentation
 */
interface AccountsRawContactsAssociationsUpdate {

    // region ASSOCIATE

    /**
     * Associates the given [rawContacts] with the given [account].
     *
     * RawContacts that were already associated with an Account will no longer be associated with
     * that Account if this call succeeds. Existing group memberships will be deleted. A group
     * membership to the default group of the given [account] will be created automatically by the
     * Contacts Provider upon successful operation.
     *
     * Only existing RawContacts that have been retrieved via a query will be processed. Those that
     * have been manually created via a constructor will be ignored.
     *
     * This operation will fail if the given [account] is not in the system or if no existing
     * RawContacts are provided.
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
     * [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
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

    /**
     * Associates all local RawContacts with the given [account].
     *
     * Local RawContacts are those that are not associated with any Account. A group membership to
     * the default group of the given [account] will be created automatically by the Contacts
     * Provider upon successful operation.
     *
     * Only existing RawContacts that have been retrieved via a query will be processed. Those that
     * have been manually created via a constructor will be ignored.
     *
     * This operation will fail if the given [account] is not in the system.
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
     * [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithLocalRawContacts(account: Account): Boolean

    /**
     * Associates / transfers the RawContacts associated with the [srcAccounts] to the [dstAccount].
     *
     * RawContacts associated with the [srcAccounts] will no longer be associated with those
     * Accounts if this call succeeds. Existing group memberships will be deleted. A group
     * membership to the default group of the given [dstAccount] will be created automatically by
     * the Contacts Provider upon successful operation.
     *
     * Only existing RawContacts that have been retrieved via a query will be processed. Those that
     * have been manually created via a constructor will be ignored.
     *
     * This operation will fail if the given [dstAccount] is not in the system or if no
     * [srcAccounts] are provided. In the case where there are no associated RawContacts with any
     * of the [srcAccounts], this operation succeeds.
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
     * [ContactsPermissions.WRITE_PERMISSION].
     * 
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account, vararg srcAccounts: Account
    ): Boolean

    /**
     * See [associateAccountWithRawContactsFromAccounts].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account, srcAccounts: Collection<Account>
    ): Boolean

    /**
     * See [associateAccountWithRawContactsFromAccounts].
     */
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
}

@Suppress("FunctionName")
internal fun AccountsRawContactsAssociationsUpdate(context: Context):
        AccountsRawContactsAssociationsUpdate = AccountsRawContactsAssociationsUpdateImpl(
    context, AccountsPermissions(context)
)

private class AccountsRawContactsAssociationsUpdateImpl(
    private val context: Context,
    private val permissions: AccountsPermissions
) : AccountsRawContactsAssociationsUpdate {

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

        if (!permissions.canUpdateRawContactsAssociations()) {
            return false
        }

        // Only existing RawContacts can be associated with an Account.
        val nonNullRawContactIds = rawContacts.map { it.id }.filterNotNull()

        if (nonNullRawContactIds.isEmpty()) {
            return false
        }

        // A valid account is required for associations.
        account.nullIfNotInSystem(context) ?: return false

        return context.contentResolver.updateRawContactsAccounts(nonNullRawContactIds, account)
    }

    override fun associateAccountWithLocalRawContacts(account: Account): Boolean {
        if (!permissions.canUpdateRawContactsAssociations()) {
            return false
        }

        // A valid account is required for associations.
        account.nullIfNotInSystem(context) ?: return false

        return context.contentResolver.updateLocalRawContactsAccounts(account)
    }

    override fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account,
        vararg srcAccounts: Account
    ): Boolean = associateAccountWithRawContactsFromAccounts(dstAccount, srcAccounts.asSequence())

    override fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account,
        srcAccounts: Collection<Account>
    ): Boolean = associateAccountWithRawContactsFromAccounts(dstAccount, srcAccounts.asSequence())

    override fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account,
        srcAccounts: Sequence<Account>
    ): Boolean {
        if (!permissions.canUpdateRawContactsAssociations() || srcAccounts.isEmpty()) {
            return false
        }

        // A valid account is required for associations.
        dstAccount.nullIfNotInSystem(context) ?: return false

        val rawContactIdsFromSrcAccounts =
            context.contentResolver.rawContactIdsAssociatedWithAccounts(srcAccounts)

        // Succeed if there are no RawContacts from the source Accounts.
        if (rawContactIdsFromSrcAccounts.isEmpty()) {
            return true
        }

        return context.contentResolver.updateRawContactsAccounts(
            rawContactIdsFromSrcAccounts,
            dstAccount
        )
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

private fun ContentResolver.updateRawContactsAccounts(
    rawContactIds: Set<Long>, account: Account
): Boolean = updateRawContactsAccounts(rawContactIds.asSequence(), account)

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
        .withSelection(RawContactsFields.Id `in` rawContactIds)
        .withValue(RawContactsFields.AccountName, account.name)
        .withValue(RawContactsFields.AccountType, account.type)
        .build()
) != null

private fun ContentResolver.updateLocalRawContactsAccounts(account: Account): Boolean = applyBatch(
    // No need to delete existing group memberships because local RawContacts are not associated
    // with an Account and therefore do not have any group memberships.

    // Update the sync columns of RawContacts without an associated Account. This does not include
    // RawContacts associated with invalid Accounts.
    newUpdate(Table.RAW_CONTACTS)
        .withSelection(
            RawContactsFields.AccountName.isNull() or RawContactsFields.AccountType.isNull()
        )
        .withValue(RawContactsFields.AccountName, account.name)
        .withValue(RawContactsFields.AccountType, account.type)
        .build()
) != null

private fun ContentResolver.rawContactIdsAssociatedWithAccounts(accounts: Sequence<Account>):
        Set<Long> = query(
    Table.RAW_CONTACTS,
    Include(RawContactsFields.Id),
    accounts.toRawContactsWhere()
) {
    val rawContactIds = mutableSetOf<Long>()
    val rawContactsCursor = it.rawContactsCursor()

    while (it.moveToNext()) {
        rawContactsCursor.rawContactId?.let(rawContactIds::add)
    }

    rawContactIds
} ?: emptySet()