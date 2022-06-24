package contacts.core.accounts

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newDelete
import android.content.ContentProviderOperation.newUpdate
import android.content.ContentResolver
import contacts.core.*
import contacts.core.accounts.AccountsLocalRawContactsUpdate.Result.FailureReason
import contacts.core.entities.ExistingRawContactEntityWithContactId
import contacts.core.entities.MimeType
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.operation.withSelection
import contacts.core.entities.operation.withValue
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.util.*

/**
 * Associates **local** RawContacts (those that are not associated with an Account) to an
 * Account to enable syncing....
 *
 * ## Permissions
 *
 * The [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and [ContactsPermissions.WRITE_PERMISSION] are
 * assumed to have been granted already in these examples for brevity. All updates will do nothing
 * if these permissions are not granted.
 *
 * ## Usage
 *
 * Here is an example that associate/add the given localRawContacts to the given account.
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = accountsLocalRawContactsUpdate
 *      .addToAccount(account)
 *      .localRawContacts(rawContacts)
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * AccountsLocalRawContactsUpdate.Result result = accountsLocalRawContactsUpdate
 *      .addToAccount(account)
 *      .localRawContacts(rawContacts)
 *      .commit();
 * ```
 */
interface AccountsLocalRawContactsUpdate : CrudApi {

    /**
     * The [Account] that will be associated with the existing local RawContacts specified in
     * [localRawContacts].
     *
     * A valid account that is in the system is required. Otherwise, the update will fail.
     */
    fun addToAccount(account: Account): AccountsLocalRawContactsUpdate

    /**
     * The existing local RawContacts that will be associated with the [Account] specified in
     * [addToAccount].
     *
     * If a RawContact is already associated with an Account, this will NOT attempt to change the
     * Account and the operation will fail.
     *
     * A group membership to the default group of the given account will be created automatically
     * by the Contacts Provider upon successful operation.
     */
    fun localRawContacts(
        vararg rawContacts: ExistingRawContactEntityWithContactId
    ): AccountsLocalRawContactsUpdate

    /**
     * See [AccountsLocalRawContactsUpdate.localRawContacts].
     */
    fun localRawContacts(
        rawContacts: Collection<ExistingRawContactEntityWithContactId>
    ): AccountsLocalRawContactsUpdate

    /**
     * See [AccountsLocalRawContactsUpdate.localRawContacts].
     */
    fun localRawContacts(
        rawContacts: Sequence<ExistingRawContactEntityWithContactId>
    ): AccountsLocalRawContactsUpdate

    /**
     * Associates all existing local RawContacts specified in [localRawContacts] to the Account
     * specified in [addToAccount].
     *
     * A valid account that is in the system is required. Otherwise, the update will fail.
     *
     * If a RawContact is already associated with an Account, this will NOT attempt to change the
     * Account and the operation will fail.
     *
     * A group membership to the default group of the given account will be created automatically
     * by the Contacts Provider upon successful operation.
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
    fun commit(): Result

    /**
     * Associates all existing local RawContacts specified in [localRawContacts] to the Account
     * specified in [addToAccount].
     *
     * A valid account that is in the system is required. Otherwise, the update will fail.
     *
     * If a RawContact is already associated with an Account, this will NOT attempt to change the
     * Account and the operation will fail.
     *
     * A group membership to the default group of the given account will be created automatically
     * by the Contacts Provider upon successful operation.
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION] and
     * [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo updates. This means that depending on when the cancellation
     * occurs, some if not all of the RawContacts in the update queue may have already been
     * updated.**
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun commit(cancel: () -> Boolean = { false }): Result
    fun commit(cancel: () -> Boolean): Result

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
    override fun redactedCopy(): AccountsLocalRawContactsUpdate

    interface Result : CrudApi.Result {

        /**
         * True if the RawContacts have been successfully associated to the given Account.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully associated with the Account.
         */
        fun isSuccessful(rawContact: ExistingRawContactEntityWithContactId): Boolean

        /**
         * Returns the reason why the update failed for this [rawContact]. Null if it did not fail.
         */
        fun failureReason(rawContact: ExistingRawContactEntityWithContactId): FailureReason?

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result

        enum class FailureReason {

            /**
             * The Account is either not specified or is not in the system.
             */
            INVALID_ACCOUNT,

            /**
             * The RawContact is already associated with an Account. It is not a local RawContact.
             * This is also used if the RawContact no longer exists (deleted).
             */
            RAW_CONTACT_IS_NOT_LOCAL,

            /**
             * The update failed because of no permissions, no RawContacts specified, etc...
             *
             * ## Dev note
             *
             * We can probably add more reasons instead of just putting all others in the "unknown"
             * bucket. We'll see if consumers need to know about other failure reasons.
             */
            UNKNOWN
        }
    }
}

@Suppress("FunctionName")
internal fun AccountsLocalRawContactsUpdate(contacts: Contacts, isProfile: Boolean):
        AccountsLocalRawContactsUpdate = AccountsLocalRawContactsUpdateImpl(
    contacts, isProfile
)

private class AccountsLocalRawContactsUpdateImpl(
    override val contactsApi: Contacts,
    private val isProfile: Boolean,

    private var account: Account? = null,
    private val rawContactIds: MutableSet<Long> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : AccountsLocalRawContactsUpdate {

    override fun toString(): String =
        """
            AccountsRawContactsAssociationsUpdate {
                isProfile: $isProfile
                account: $account
                rawContactIds: $rawContactIds
                hasPermission: ${accountsPermissions.canUpdateLocalRawContactsAccount()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): AccountsLocalRawContactsUpdate =
        AccountsLocalRawContactsUpdateImpl(
            contactsApi, isProfile,

            // Redact account info
            account?.redactedCopy(),
            rawContactIds,

            isRedacted = true
        )

    override fun addToAccount(account: Account): AccountsLocalRawContactsUpdate = apply {
        this.account = account.redactedCopyOrThis(isRedacted)
    }

    override fun localRawContacts(vararg rawContacts: ExistingRawContactEntityWithContactId) =
        localRawContacts(rawContacts.asSequence())

    override fun localRawContacts(rawContacts: Collection<ExistingRawContactEntityWithContactId>) =
        localRawContacts(rawContacts.asSequence())

    override fun localRawContacts(
        rawContacts: Sequence<ExistingRawContactEntityWithContactId>
    ): AccountsLocalRawContactsUpdate = apply {
        rawContactIds.addAll(rawContacts.map { it.id })
    }

    override fun commit() = commit { false }

    override fun commit(cancel: () -> Boolean): AccountsLocalRawContactsUpdate.Result {
        onPreExecute()

        val account = account
        return if (
            rawContactIds.isEmpty()
            || !accountsPermissions.canUpdateLocalRawContactsAccount()
            || cancel()
        ) {
            AccountsRawContactsAssociationsUpdateResultFailed(FailureReason.UNKNOWN)
        } else if (account?.isInSystem(contactsApi.accounts()) != true) {
            // Either account was not specified (null) or it is not in system.
            AccountsRawContactsAssociationsUpdateResultFailed(FailureReason.INVALID_ACCOUNT)
        } else {
            val failureReasons = mutableMapOf<Long, FailureReason>()
            for (rawContactId in rawContactIds) {
                if (cancel() || rawContactId.isProfileId != isProfile) {
                    failureReasons[rawContactId] = FailureReason.UNKNOWN
                } else if (contentResolver.rawContactHasAccount(rawContactId)) {
                    failureReasons[rawContactId] = FailureReason.RAW_CONTACT_IS_NOT_LOCAL
                } else if (!contentResolver.setRawContactAccount(account, rawContactId)) {
                    failureReasons[rawContactId] = FailureReason.UNKNOWN
                }
                // else operation succeeded. No Failure reason.
            }
            AccountsRawContactsAssociationsUpdateResult(failureReasons)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

/**
 * Deletes existing group memberships in the Data table matching for the RawContact with the given
 * [rawContactId] (if any). Then updates the sync columns in the RawContacts table for the
 * RawContact with the given [rawContactId] with values from the given [account]. These two
 * operations are done in a batch so either both succeed or both fail.
 *
 * Note that local RawContacts may have a group membership to an Account that it is not associated
 * with. Therefore, we need to delete that membership.
 */
private fun ContentResolver.setRawContactAccount(
    account: Account, rawContactId: Long
): Boolean = applyBatch(
    arrayListOf<ContentProviderOperation>().apply {
        // First delete existing group memberships.
        newDelete(if (rawContactId.isProfileId) ProfileUris.DATA.uri else Table.Data.uri)
            .withSelection(
                (Fields.RawContact.Id equalTo rawContactId)
                        and (Fields.MimeType equalTo MimeType.GroupMembership)
            )
            .build()
            .let(::add)

        // Then update the sync columns.
        newUpdate(if (rawContactId.isProfileId) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri)
            .withSelection(RawContactsFields.Id equalTo rawContactId)
            .withValue(RawContactsFields.AccountName, account.name)
            .withValue(RawContactsFields.AccountType, account.type)
            .build()
            .let(::add)
    }
) != null

private fun ContentResolver.rawContactHasAccount(rawContactId: Long): Boolean = query(
    if (rawContactId.isProfileId) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
    Include(RawContactsFields.Id),
    RawContactsFields.run {
        Id.equalTo(rawContactId) and
                AccountName.isNotNullOrEmpty() and
                AccountType.isNotNullOrEmpty()
    }
) {
    val matchingRawContactId: Long? = it.getNextOrNull { it.rawContactsCursor().rawContactId }
    matchingRawContactId == rawContactId
} ?: false


private class AccountsRawContactsAssociationsUpdateResult private constructor(
    private val failureReasons: Map<Long, FailureReason>,
    override val isRedacted: Boolean
) : AccountsLocalRawContactsUpdate.Result {

    constructor(failureReasons: Map<Long, FailureReason>) : this(
        failureReasons = failureReasons,
        isRedacted = false
    )

    override fun toString(): String =
        """
            AccountsLocalRawContactsUpdate.Result {
                isSuccessful: $isSuccessful
                failureReasons: $failureReasons
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): AccountsLocalRawContactsUpdate.Result =
        AccountsRawContactsAssociationsUpdateResult(
            failureReasons = failureReasons,
            isRedacted = true
        )

    override val isSuccessful: Boolean by unsafeLazy { failureReasons.isEmpty() }

    override fun isSuccessful(rawContact: ExistingRawContactEntityWithContactId): Boolean =
        failureReason(rawContact) == null

    override fun failureReason(rawContact: ExistingRawContactEntityWithContactId): FailureReason? =
        failureReasons[rawContact.id]
}

private class AccountsRawContactsAssociationsUpdateResultFailed private constructor(
    private val failureReason: FailureReason,
    override val isRedacted: Boolean
) : AccountsLocalRawContactsUpdate.Result {

    constructor(failureReason: FailureReason) : this(
        failureReason = failureReason,
        isRedacted = false
    )

    override fun toString(): String =
        """
            AccountsLocalRawContactsUpdate.Result {
                isSuccessful: $isSuccessful
                failureReason: $failureReason
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): AccountsLocalRawContactsUpdate.Result =
        AccountsRawContactsAssociationsUpdateResultFailed(
            failureReason = failureReason,
            isRedacted = true
        )

    override val isSuccessful: Boolean = false

    override fun isSuccessful(
        rawContact: ExistingRawContactEntityWithContactId
    ): Boolean = isSuccessful

    override fun failureReason(
        rawContact: ExistingRawContactEntityWithContactId
    ): FailureReason = failureReason
}