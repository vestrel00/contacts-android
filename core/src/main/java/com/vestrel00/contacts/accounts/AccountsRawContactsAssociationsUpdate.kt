package com.vestrel00.contacts.accounts

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newDelete
import android.content.ContentProviderOperation.newUpdate
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.entities.cursor.rawContactsCursor
import com.vestrel00.contacts.entities.operation.withSelection
import com.vestrel00.contacts.entities.operation.withValue
import com.vestrel00.contacts.entities.table.ProfileUris
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.util.applyBatch
import com.vestrel00.contacts.util.isNotInSystem
import com.vestrel00.contacts.util.query

/**
 * Updates Profile OR non-Profile (depending on instance) RawContacts associations to Accounts.
 *
 * Due to certain limitations and behaviors imposed by the Contacts Provider, this only supports;
 *
 * - Associate local RawContacts (those that are not associated with an Account) to an Account,
 *   allowing syncing between devices.
 *
 * This does not support;
 *
 * - Dissociate RawContacts from their Account such that they remain local to the device and not
 *   synced between devices.
 * - Transfer RawContacts from one Account to another.
 *
 * Although, there is a way to implement it if the community strongly desires these features.
 *
 * Read the **SyncColumns modifications** section of the DEV_NOTES for more details.
 *
 * ## Permissions
 *
 * The [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and [ContactsPermissions.WRITE_PERMISSION] are
 * assumed to have been granted already in these examples for brevity. All updates will do nothing
 * if these permissions are not granted.
 *
 * ## Usage
 *
 * To associate the given localRawContacts to the given account;
 *
 * ```kotlin
 * val result = accountsRawContactsAssociationsUpdate
 *      .associateAccountWithLocalRawContacts(account, localRawContacts)
 *      .commit()
 * ```
 *
 * To associate all local RawContacts to the given account;
 *
 * ```kotlin
 * val result = accountsRawContactsAssociationsUpdate
 *      .associateAccountWithAllLocalRawContacts(account)
 *      .commit()
 * ```
 */
interface AccountsRawContactsAssociationsUpdate {

    // region ASSOCIATE

    /**
     * Associates the given **local** [rawContacts] with the given [account]. Local RawContacts are
     * not associated with an Account. Non-local RawContacts will be filtered out and not be
     * associated with the [account].
     *
     * A group membership to the default group of the given [account] will be created automatically
     * by the Contacts Provider upon successful operation.
     *
     * Only existing local RawContacts that have been retrieved via a query will be processed. Those
     * that have been manually created via a constructor will be ignored.
     *
     * This operation will fail if the given [account] is not in the system. In the case where there
     * are no local RawContacts, this operation succeeds.
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
    fun associateAccountWithLocalRawContacts(
        account: Account, vararg rawContacts: RawContactEntity
    ): Boolean

    /**
     * See [associateAccountWithLocalRawContacts].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithLocalRawContacts(
        account: Account, rawContacts: Collection<RawContactEntity>
    ): Boolean

    /**
     * See [associateAccountWithLocalRawContacts].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun associateAccountWithLocalRawContacts(
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
     * This operation will fail if the given [account] is not in the system. In the case where there
     * are no local RawContacts, this operation succeeds.
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
    fun associateAccountWithAllLocalRawContacts(account: Account): Boolean

    /*
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
     * Associates / transfers the RawContacts associated with the [srcAccounts] to the [dstAccount].
     *
     * RawContacts associated with the [srcAccounts] will no longer be associated with those
     * Accounts if this call succeeds. Existing group memberships will be deleted. A group
     * membership to the default group of the given [dstAccount] will be created automatically by
     * the Contacts Provider upon successful operation.
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

    /**
     * Associates / transfers the RawContacts associated with any Account to the [dstAccount].
     * This does not include local RawContacts, which are not associated with an Account.
     *
     * RawContacts associated with an Account will no longer be associated with those Accounts if
     * this call succeeds. Existing group memberships will be deleted. A group membership to the
     * default group of the given [dstAccount] will be created automatically by the Contacts
     * Provider upon successful operation.
     *
     * This operation will fail if the given [dstAccount] is not in the system. In the case where
     * there are no associated RawContacts with any existing Accounts, this operation succeeds.
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
    fun associateAccountWithRawContactsFromAllAccounts(dstAccount: Account): Boolean

    /**
     * Associates / transfers all RawContacts from all Accounts including local RawContacts to the
     * [dstAccount].
     *
     * RawContacts associated with an Account will no longer be associated with those Accounts if
     * this call succeeds. Existing group memberships will be deleted. A group membership to the
     * default group of the given [dstAccount] will be created automatically by the Contacts
     * Provider upon successful operation.
     *
     * This operation will fail if the given [dstAccount] is not in the system. In the case where
     * there are no RawContacts, this operation succeeds.
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
    fun associateAccountWithAllRawContacts(dstAccount: Account): Boolean
     */

    // endregion

    // region DISSOCIATE

    /*
    /**
     * Dissociates the given [rawContacts] from associations with any Account.
     *
     * RawContacts that were already associated with an Account will no longer be associated with
     * that Account if this call succeeds. Existing group memberships will be retained. RawContacts
     * not associated with an Account are local to the device.
     *
     * Only existing RawContacts that have been retrieved via a query will be processed. Those that
     * have been manually created via a constructor will be ignored.
     *
     * This operation will fail if no existing RawContacts are provided.
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
    fun dissociateRawContacts(vararg rawContacts: RawContactEntity): Boolean

    /**
     * See [dissociateRawContacts].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContacts(rawContacts: Collection<RawContactEntity>): Boolean

    /**
     * See [dissociateRawContacts].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContacts(rawContacts: Sequence<RawContactEntity>): Boolean

    /**
     * Dissociates the RawContacts associated with any of the given [accounts].
     *
     * RawContacts associated with any of the [accounts] will no longer be associated with those
     * Accounts if this call succeeds. Existing group memberships will be retained. RawContacts
     * not associated with an Account are local to the device.
     *
     * In the case where there are no associated RawContacts with any of the [accounts], this
     * operation succeeds.
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
    fun dissociateRawContactsFromAccounts(vararg accounts: Account): Boolean

    /**
     * See [dissociateRawContactsFromAccounts].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContactsFromAccounts(accounts: Collection<Account>): Boolean

    /**
     * See [dissociateRawContactsFromAccounts].
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun dissociateRawContactsFromAccounts(accounts: Sequence<Account>): Boolean

    /**
     * Dissociates the RawContacts associated with any Account.
     *
     * RawContacts associated with any Account will no longer be associated with those Accounts if
     * this call succeeds. Existing group memberships will be retained. RawContacts not associated
     * with an Account are local to the device.
     *
     * In the case where there are no associated RawContacts with any Account, this operation
     * succeeds.
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
    fun dissociateRawContactsFromAllAccounts(): Boolean
     */

    // endregion
}

@Suppress("FunctionName")
internal fun AccountsRawContactsAssociationsUpdate(context: Context, isProfile: Boolean):
        AccountsRawContactsAssociationsUpdate = AccountsRawContactsAssociationsUpdateImpl(
    context.applicationContext, AccountsPermissions(context), isProfile
)

private class AccountsRawContactsAssociationsUpdateImpl(
    private val applicationContext: Context,
    private val permissions: AccountsPermissions,
    private val isProfile: Boolean
) : AccountsRawContactsAssociationsUpdate {

    override fun toString(): String =
        """
            AccountsRawContactsAssociationsUpdate {
                isProfile: $isProfile
            }
        """.trimIndent()

    // region ASSOCIATE

    override fun associateAccountWithLocalRawContacts(
        account: Account, vararg rawContacts: RawContactEntity
    ) = associateAccountWithLocalRawContacts(account, rawContacts.asSequence())

    override fun associateAccountWithLocalRawContacts(
        account: Account, rawContacts: Collection<RawContactEntity>
    ) = associateAccountWithLocalRawContacts(account, rawContacts.asSequence())

    override fun associateAccountWithLocalRawContacts(
        account: Account, rawContacts: Sequence<RawContactEntity>
    ): Boolean {
        if (!permissions.canUpdateRawContactsAssociations() ||
            account.isNotInSystem(applicationContext)
        ) {
            return false
        }

        if (rawContacts.find { it.isProfile != isProfile } != null) {
            // Immediately fail if there one or more RawContacts that does not match isProfile.
            return false
        }

        val rawContactIds = rawContacts.mapNotNull { it.id }
        val localRawContactIds = applicationContext.contentResolver.rawContactIdsWhere(
            // Not using and/or as infix because this formatting looks better in this case.
            (RawContactsFields.Id `in` rawContactIds)
                .and(
                    RawContactsFields.AccountName.isNull()
                        .or(RawContactsFields.AccountType.isNull())
                ),
            isProfile
        )

        // Succeed if there are no local RawContacts.
        // Using the || operator here is important because if it is true, then the update does
        // not occur. If && is used instead, the update will occur even if it is true.
        return localRawContactIds.isEmpty() || applicationContext.contentResolver
            .updateRawContactsAccount(account, localRawContactIds, isProfile)
    }

    override fun associateAccountWithAllLocalRawContacts(account: Account): Boolean {
        if (!permissions.canUpdateRawContactsAssociations() ||
            account.isNotInSystem(applicationContext)
        ) {
            return false
        }

        val localRawContactIds = applicationContext.contentResolver.rawContactIdsWhere(
            RawContactsFields.AccountName.isNull() or RawContactsFields.AccountType.isNull(),
            isProfile
        )

        // Succeed if there are no local RawContacts.
        // Using the || operator here is important because if it is true, then the update does
        // not occur. If && is used instead, the update will occur even if it is true.
        return localRawContactIds.isEmpty() || applicationContext.contentResolver
            .updateRawContactsAccount(account, localRawContactIds, isProfile)
    }

    // NOTE: If uncommenting code below, update it with isProfile.

    /*
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
        val nonNullRawContactIds = rawContacts.mapNotNull { it.id }

        return nonNullRawContactIds.isNotEmpty()
                && permissions.canUpdateRawContactsAssociations()
                && account.isInSystem(context)
                && context.contentResolver.updateRawContactsAccount(account, nonNullRawContactIds)
    }

    override fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account,
        vararg srcAccounts: Account
    ) = associateAccountWithRawContactsFromAccounts(dstAccount, srcAccounts.asSequence())

    override fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account,
        srcAccounts: Collection<Account>
    ) = associateAccountWithRawContactsFromAccounts(dstAccount, srcAccounts.asSequence())

    override fun associateAccountWithRawContactsFromAccounts(
        dstAccount: Account,
        srcAccounts: Sequence<Account>
    ): Boolean {
        if (!permissions.canUpdateRawContactsAssociations()
            || srcAccounts.isEmpty()
            || dstAccount.isNotInSystem(context)
        ) {
            return false
        }

        val rawContactIdsFromSrcAccounts =
            context.contentResolver.rawContactIdsWhere(srcAccounts.toRawContactsWhere())

        // Succeed if there are no RawContacts from the source Accounts.
        // Using the || operator here is important because if it is true, then the update does
        // not occur. If && is used instead, the update will occur even if it is true.
        return rawContactIdsFromSrcAccounts.isEmpty() || context.contentResolver
            .updateRawContactsAccount(dstAccount, rawContactIdsFromSrcAccounts)
    }

    override fun associateAccountWithRawContactsFromAllAccounts(dstAccount: Account): Boolean {
        if (!permissions.canUpdateRawContactsAssociations() || dstAccount.isNotInSystem(context)) {
            return false
        }

        val rawContactIdsAssociatedWithAnAccount = context.contentResolver.rawContactIdsWhere(
            RawContactsFields.AccountName.isNotNull() and RawContactsFields.AccountType.isNotNull()
        )

        // Succeed if there are no RawContacts associated with any Account.
        // Using the || operator here is important because if it is true, then the update does
        // not occur. If && is used instead, the update will occur even if it is true.
        return rawContactIdsAssociatedWithAnAccount.isEmpty() || context.contentResolver
            .updateRawContactsAccount(dstAccount, rawContactIdsAssociatedWithAnAccount)
    }

    override fun associateAccountWithAllRawContacts(dstAccount: Account): Boolean =
        permissions.canUpdateRawContactsAssociations()
                && dstAccount.isInSystem(context)
                && context.contentResolver.updateRawContactsAccount(
            dstAccount,
            // Delete all group memberships.
            Fields.MimeType equalTo MimeType.GROUP_MEMBERSHIP,
            // Associate all existing RawContacts.
            RawContactsFields.ContactId.isNotNull()
        )
     */

    // endregion

    // region DISSOCIATE

    /*
    override fun dissociateRawContacts(vararg rawContacts: RawContactEntity) =
        dissociateRawContacts(rawContacts.asSequence())

    override fun dissociateRawContacts(rawContacts: Collection<RawContactEntity>) =
        dissociateRawContacts(rawContacts.asSequence())

    override fun dissociateRawContacts(rawContacts: Sequence<RawContactEntity>): Boolean {
        // Only existing RawContacts can be processed.
        val nonNullRawContactIds = rawContacts.mapNotNull { it.id }

        return nonNullRawContactIds.isNotEmpty()
                && permissions.canUpdateRawContactsAssociations()
                && context.contentResolver.updateRawContactsAccount(null, nonNullRawContactIds)
    }

    override fun dissociateRawContactsFromAccounts(vararg accounts: Account) =
        dissociateRawContactsFromAccounts(accounts.asSequence())

    override fun dissociateRawContactsFromAccounts(accounts: Collection<Account>) =
        dissociateRawContactsFromAccounts(accounts.asSequence())

    override fun dissociateRawContactsFromAccounts(accounts: Sequence<Account>): Boolean {
        if (!permissions.canUpdateRawContactsAssociations() || accounts.isEmpty()) {
            return false
        }

        val rawContactIdsFromAccounts =
            context.contentResolver.rawContactIdsWhere(accounts.toRawContactsWhere())

        // Succeed if there are no RawContacts from the source Accounts.
        // Using the || operator here is important because if it is true, then the update does
        // not occur. If && is used instead, the update will occur even if it is true.
        return rawContactIdsFromAccounts.isEmpty() || context.contentResolver
            .updateRawContactsAccount(null, rawContactIdsFromAccounts)
    }

    override fun dissociateRawContactsFromAllAccounts() =
        permissions.canUpdateRawContactsAssociations()
                && context.contentResolver.updateRawContactsAccount(
            null, null,
            // Dissociate all existing RawContacts.
            RawContactsFields.ContactId.isNotNull()
        )
     */

    // endregion
}

private fun ContentResolver.updateRawContactsAccount(
    account: Account?, rawContactIds: Set<Long>, isProfile: Boolean
): Boolean = updateRawContactsAccount(account, rawContactIds.asSequence(), isProfile)

private fun ContentResolver.updateRawContactsAccount(
    account: Account?, rawContactIds: Sequence<Long>, isProfile: Boolean
): Boolean = updateRawContactsAccount(
    account,
    (Fields.RawContact.Id `in` rawContactIds)
            and (Fields.MimeType equalTo MimeType.GROUP_MEMBERSHIP),
    RawContactsFields.Id `in` rawContactIds,
    isProfile
)

/**
 * Deletes existing group memberships in the Data table matching [dataWhere] and then updates the
 * sync columns in the RawContacts table matching [rawContactsWhere] with the given [account]. These
 * two operations are done in a batch so either both succeed or both fail.
 *
 * If [account] is null, then no delete operation will be done in the Data table because group
 * memberships to at least the default group must be retained. Otherwise, the RawContacts will not
 * be part of a visible group resulting in these RawContacts to not show up in the native Contacts
 * app.
 */
private fun ContentResolver.updateRawContactsAccount(
    account: Account?,
    dataWhere: Where<AbstractDataField>?,
    rawContactsWhere: Where<RawContactsField>,
    isProfile: Boolean
): Boolean = applyBatch(
    arrayListOf<ContentProviderOperation>().apply {
        // First delete existing group memberships.
        if (account != null && dataWhere != null) {
            newDelete(if (isProfile) ProfileUris.DATA.uri else Table.Data.uri)
                .withSelection(dataWhere)
                .build()
                .let(::add)
        }

        // Then update the sync columns.
        newUpdate(if (isProfile) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri)
            .withSelection(rawContactsWhere)
            .withValue(RawContactsFields.AccountName, account?.name)
            .withValue(RawContactsFields.AccountType, account?.type)
            .build()
            .let(::add)
    }
) != null

private fun ContentResolver.rawContactIdsWhere(
    where: Where<RawContactsField>?,
    isProfile: Boolean
): Set<Long> = query(
    if (isProfile) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
    Include(RawContactsFields.Id),
    where
) {
    val rawContactIds = mutableSetOf<Long>()
    val rawContactsCursor = it.rawContactsCursor()

    while (it.moveToNext()) {
        rawContactsCursor.rawContactId?.let(rawContactIds::add)
    }

    rawContactIds
} ?: emptySet()