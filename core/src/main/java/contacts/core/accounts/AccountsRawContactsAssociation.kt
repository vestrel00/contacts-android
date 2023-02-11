package contacts.core.accounts

import android.accounts.Account
import contacts.core.ContactsPermissions
import contacts.core.CrudApi
import contacts.core.Redactable
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.ExistingSimContactEntity
import contacts.core.util.redactedCopy

/**
 * Associates RawContacts to specified Accounts. RawContacts that are associated with a non-null
 * valid [Account] will have syncing enabled depending on system Account settings. RawContacts that
 * are not associated with an Account are local to the device and will not be synced.
 *
 * ## How does this work?
 *
 * This API does the same things as the Google Contacts app. The functions of this API is based off
 * of the Google Contacts app.
 *
 * Depending on the current Account and target Account of the [ExistingRawContactEntity], the
 * following will occur...
 *
 * #### A. Current Account is null and target Account is not null.
 *
 * This API will change the values of the account_name and account_type on the RawContact's table
 * row to that of the target Account.
 *
 * As a result, the following takes place...
 *
 * - The parent Contact lookup key changes after a sync is performed by the system.
 *   - Existing shortcuts to the Contact using the lookup key will no longer be valid.
 * - A group membership to the default group of the given account will be created automatically by
 *   the Contacts Provider upon successful operation.
 * - Group memberships from the current account prior to the operation is not
 *   automatically deleted by the Contacts Provider. This API does that manually.
 *
 * There are no changes to anything else. All rows in the Data table also remain the same.
 *
 * TODO verify this ^
 * TODO are group memberships from the current account carried over to the target account for
 * similar groups (if exist)?
 *
 * #### B. Current Account is not null and target Account is null.
 *
 * This API inserts a copy of the RawContact (associated to the target Account) and all of its Data
 * into the database. The original RawContact and all of its Data are deleted from the database.
 *
 * As a result, the following takes place...
 *
 * - The parent Contact lookup key changes after a sync is performed by the system.
 *   - Existing shortcuts to the Contact using the lookup key will no longer be valid.
 * - A group membership to the default group of the given account will be created automatically by
 *   the Contacts Provider upon successful operation.
 * - Group memberships from the current account prior to the operation will not be carried over to
 *   the RawContact copy.
 *
 * TODO verify this ^
 * TODO are group memberships from the current account carried over to the target account for
 * similar groups (if exist)?
 *
 * #### C. Current Account is not null and target Account is also not null.
 *
 * Same as "B. Current Account is not null and target Account is null."
 *
 * TODO verify this ^
 * TODO are group memberships from the current account carried over to the target account for
 * similar groups (if exist)?
 *
 * #### D. Current Account (null or not) is the same as the target Account.
 *
 * No operation is performed here. The current Account is already the target Account. Result is
 * success.
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
    fun associate(rawContacts: Collection<Entry>)

    /**
     * See [AccountsRawContactsAssociation.associate].
     */
    fun associate(rawContacts: Sequence<Entry>)

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
         * True if all RawContacts have been successfully associated to the target Accounts.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully associated with the target Account.
         */
        fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean

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
             * The associated failed because of no permissions, no RawContacts specified, etc...
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