package contacts.core.accounts

import android.accounts.Account
import contacts.core.ContactsPermissions
import contacts.core.CrudApi
import contacts.core.Redactable
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.ExistingSimContactEntity
import contacts.core.util.redactedCopy

/**
 * Associates/moves RawContacts from one Account to another.
 *
 * This API functions similarly to the Google Contacts app. Copies of RawContacts are inserted
 * into the database under a different account and the original RawContacts are deleted afterwards.
 * RawContact and Data values are carried over.
 *
 * In other words, this is a copy-paste-delete operation. New rows are created in the RawContact,
 * Contact, and Data tables with the same values from the original. Then, the original rows are
 * deleted.
 *
 * Memberships to Groups (which are Account-based) are "carried over" on a best-effort basis;
 *
 * - Groups with matching title (case-sensitive)
 * - Default Group (autoAdd is true)
 * - Favorites Group (if starred is true)
 *
 * Contact IDs and lookup keys will change. This means that references to Contact IDs and lookup
 * keys will become invalid. For example, shortcuts may break after performing this operation.
 *
 * **Profile RawContacts are not supported!** Operations for these will fail.
 *
 * ## Permissions
 *
 * The [AccountsPermissions.GET_ACCOUNTS_PERMISSION], [ContactsPermissions.READ_PERMISSION], and
 * [ContactsPermissions.WRITE_PERMISSION] are assumed to have been granted already in these
 * examples for brevity. All updates will do nothing if these permissions are not granted.
 *
 * ## Usage
 *
 * Here is an example that associates the given rawContacts to the given account.
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = accountsRawContactsAssociation
 *      .associateRawContactsTo(account, rawContacts)
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * AccountsRawContactsAssociation.Result result = accountsRawContactsAssociation
 *      .associateRawContactsTo(account, rawContacts)
 *      .commit();
 * ```
 */
interface AccountsRawContactsAssociation : CrudApi {

    /**
     * Adds an [Entry], which consists of [Entry.rawContactId] and [Entry.targetAccount], to the
     * queue. On [commit], each existing RawContact with [Entry.rawContactId] will be associated
     * with the [Entry.targetAccount].
     *
     * For more info, read the class documentation of [AccountsRawContactsAssociation].
     */
    fun associate(vararg entry: Entry)

    /**
     * See [AccountsRawContactsAssociation.associate].
     */
    fun associate(entry: Collection<Entry>)

    /**
     * See [AccountsRawContactsAssociation.associate].
     */
    fun associate(entry: Sequence<Entry>)

    /**
     * Creates an [Entry] for each RawContact in [rawContacts] with the [account] and passes it on
     * to [AccountsRawContactsAssociation.associate].
     */
    fun associateRawContactsTo(account: Account?, vararg rawContacts: ExistingRawContactEntity)

    /**
     * See [AccountsRawContactsAssociation.associateRawContactsTo].
     */
    fun associateRawContactsTo(account: Account?, rawContacts: Collection<ExistingRawContactEntity>)

    /**
     * See [AccountsRawContactsAssociation.associateRawContactsTo].
     */
    fun associateRawContactsTo(account: Account?, rawContacts: Sequence<ExistingRawContactEntity>)

    /**
     * Creates an [Entry] for each RawContact with the given ids in [rawContactIds] with the
     * [account] and passes it on to [AccountsRawContactsAssociation.associate].
     */
    fun associateRawContactsWithIdsTo(account: Account?, vararg rawContactIds: Long)

    /**
     * See [AccountsRawContactsAssociation.associateRawContactsWithIdsTo].
     */
    fun associateRawContactsWithIdsTo(account: Account?, rawContactIds: Collection<Long>)

    /**
     * See [AccountsRawContactsAssociation.associateRawContactsWithIdsTo].
     */
    fun associateRawContactsWithIdsTo(account: Account?, rawContactIds: Sequence<Long>)

    /**
     * Associates the given existing RawContacts to the target Account.
     *
     * For more info, read the class documentation of [AccountsRawContactsAssociation].
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION],
     * [ContactsPermissions.READ_PERMISSION], and [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): AccountsLocalRawContactsUpdate.Result

    /**
     * Associates the given existing RawContacts to the target Account.
     *
     * For more info, read the class documentation of [AccountsRawContactsAssociation].
     *
     * ## Permissions
     *
     * Requires [AccountsPermissions.GET_ACCOUNTS_PERMISSION],
     * [ContactsPermissions.READ_PERMISSION], and [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo the operation. This means that depending on when the cancellation
     * occurs, some if not all of the operations in the queue may have already been commited.**
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun commit(cancel: () -> Boolean = { false }): Result
    fun commit(cancel: () -> Boolean): AccountsLocalRawContactsUpdate.Result

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

    // We could use a Pair but it's too generic. We have more control this way.
    data class Entry internal constructor(

        /**
         * The [ExistingSimContactEntity.id] that will be associated with the [targetAccount].
         */
        val rawContactId: Long,

        /**
         * The [Account] that will be associated with the RawContact with the given [rawContactId].
         */
        val targetAccount: Account?,

        override val isRedacted: Boolean
    ) : Redactable {

        constructor(rawContact: ExistingSimContactEntity, targetAccount: Account?) :
                this(rawContact.id, targetAccount, false)

        constructor(rawContactId: Long, targetAccount: Account?) :
                this(rawContactId, targetAccount, false)

        override fun redactedCopy() = copy(
            rawContactId = rawContactId,
            targetAccount = targetAccount?.redactedCopy(),

            isRedacted = true
        )
    }

    interface Result : CrudApi.Result {

        /**
         * The list of IDs of successfully created RawContacts.
         */
        val rawContactIds: List<Long>

        /**
         * True if all RawContacts have been successfully associated to the target Accounts.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully associated with the target Account.
         */
        fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean

        /**
         * Returns the ID of the newly created RawContact, which is a copy of the given
         * [rawContact]. Use the ID to get the newly created RawContact via a query.
         *
         * Returns null if the insert operation failed.
         */
        fun rawContactId(rawContact: ExistingRawContactEntity): Long?

        /**
         * Returns the reason why the association failed for this [rawContact].
         * Null if it did not fail.
         */
        fun failureReason(rawContact: ExistingRawContactEntity): Result.FailureReason?

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result

        enum class FailureReason {

            /**
             * The Account is not in the system.
             */
            INVALID_ACCOUNT,

            /**
             * The RawContact may have been deleted prior to this operation or an incorrect
             * RawContact ID was provided.
             */
            RAW_CONTACT_NOT_FOUND,

            /**
             * Inserting a copy of a RawContact to a different Account failed.
             */
            INSERT_RAW_CONTACT_COPY_FAILED,

            /**
             * A copy of the RawContact was inserted to the target Account but deleting the
             * original RawContact failed.
             */
            DELETE_ORIGINAL_RAW_CONTACT_FAILED,

            /**
             * The operation failed because of no permissions, RawContact is used as Profile, etc...
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