package contacts.core.accounts

import android.accounts.Account
import contacts.core.*
import contacts.core.accounts.MoveRawContactsAcrossAccounts.Entry
import contacts.core.accounts.MoveRawContactsAcrossAccounts.Result
import contacts.core.accounts.MoveRawContactsAcrossAccounts.Result.FailureReason
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.ExistingSimContactEntity
import contacts.core.entities.Group
import contacts.core.entities.RawContact
import contacts.core.util.*

/**
 * Moves RawContacts from one Account to another, including a null Account.
 *
 * This API functions identically to the Google Contacts app. Copies of RawContacts are inserted
 * into the Contacts Provider database under a different account and the original RawContacts are
 * deleted afterwards. RawContact and Data values are also copied over.
 *
 * In other words, this is a copy-insert-delete operation. New rows are created in the RawContact,
 * Contact, and Data tables with the same values from the original. Then, the original rows are
 * deleted.
 *
 * **Group memberships** (which are Account-based) are "copied over" on a best-effort basis;
 *
 * - Groups with matching title (case-sensitive)
 * - Default Group (autoAdd is true)
 * - Favorites Group (if starred is true)
 *
 * **Default/primary** flags of Data rows are not copied over. For example, if a phone number is set
 * as the default (isPrimary: 1, isSuperPrimary: 1), after this move operation it will no longer
 * be a default data (isPrimary: 0,	isSuperPrimary: 0). _Yes, like all other behaviors of this API,
 * this is the same as Google Contacts._
 *
 * Contact **IDs** and **lookup keys** may change. This means that references to Contact IDs and
 * lookup keys may become invalid. For example, shortcuts may break after performing this
 * operation.
 *
 * **(Raw)Contact links (AggregationExceptions)** a copied over. For example, if there are two
 * or more RawContacts that are linked to the same Contact, moving one or more of the RawContacts
 * will still result in the RawContacts being linked to the same Contact (though the original
 * Contact may have been deleted and replaced with a new copy).
 *
 * **Custom data** are supported.
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
 * Here is an example that moves the given rawContacts to the provided Account...
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = move
 *      .rawContactsTo(account, rawContacts)
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * MoveRawContactsAcrossAccounts.Result result = move
 *      .rawContactsTo(account, rawContacts)
 *      .commit();
 * ```
 */
interface MoveRawContactsAcrossAccounts : CrudApi {

    /**
     * Adds an [Entry], which consists of [Entry.rawContactId] and [Entry.targetAccount], to the
     * queue. On [commit], each existing RawContact with [Entry.rawContactId] will be moved to the
     * [Entry.targetAccount].
     *
     * For more info, read the class documentation of [MoveRawContactsAcrossAccounts].
     */
    fun rawContacts(vararg entries: Entry): MoveRawContactsAcrossAccounts

    /**
     * See [MoveRawContactsAcrossAccounts.rawContacts].
     */
    fun rawContacts(entries: Collection<Entry>): MoveRawContactsAcrossAccounts

    /**
     * See [MoveRawContactsAcrossAccounts.rawContacts].
     */
    fun rawContacts(entries: Sequence<Entry>): MoveRawContactsAcrossAccounts

    /**
     * Creates an [Entry] for each RawContact in [rawContacts] with the [account] and passes it on
     * to [MoveRawContactsAcrossAccounts.rawContactsTo].
     */
    fun rawContactsTo(
        account: Account?,
        vararg rawContacts: ExistingRawContactEntity
    ): MoveRawContactsAcrossAccounts

    /**
     * See [MoveRawContactsAcrossAccounts.rawContactsTo].
     */
    fun rawContactsTo(
        account: Account?,
        rawContacts: Collection<ExistingRawContactEntity>
    ): MoveRawContactsAcrossAccounts

    /**
     * See [MoveRawContactsAcrossAccounts.rawContactsTo].
     */
    fun rawContactsTo(
        account: Account?,
        rawContacts: Sequence<ExistingRawContactEntity>
    ): MoveRawContactsAcrossAccounts

    /**
     * Creates an [Entry] for each RawContact with the given ids in [rawContactIds] with the
     * [account] and passes it on to [MoveRawContactsAcrossAccounts.rawContactsTo].
     */
    fun rawContactsWithIdsTo(
        account: Account?,
        vararg rawContactIds: Long
    ): MoveRawContactsAcrossAccounts

    /**
     * See [MoveRawContactsAcrossAccounts.rawContactsWithIdsTo].
     */
    fun rawContactsWithIdsTo(
        account: Account?,
        rawContactIds: Collection<Long>
    ): MoveRawContactsAcrossAccounts

    /**
     * See [MoveRawContactsAcrossAccounts.rawContactsWithIdsTo].
     */
    fun rawContactsWithIdsTo(
        account: Account?,
        rawContactIds: Sequence<Long>
    ): MoveRawContactsAcrossAccounts

    /**
     * Moves the given existing RawContacts to the target Account.
     *
     * For more info, read the class documentation of [MoveRawContactsAcrossAccounts].
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
    fun commit(): Result

    /**
     * Moves the given existing RawContacts to the target Account.
     *
     * For more info, read the class documentation of [MoveRawContactsAcrossAccounts].
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
     * occurs, some if not all of the operations in the queue may have already been committed.**
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
    override fun redactedCopy(): MoveRawContactsAcrossAccounts

    // We could use a Pair but it's too generic. We have more control this way.
    data class Entry internal constructor(

        /**
         * The [ExistingSimContactEntity.id] of the RawContact that will be moved to the
         * [targetAccount].
         */
        val rawContactId: Long,

        /**
         * The [Account] that the RawContact with the given [rawContactId] will be moved to.
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
         * True if all RawContacts have been successfully moved to the target Accounts.
         */
        val isSuccessful: Boolean

        /**
         * True if the [originalRawContact] has been successfully moved to the target Account.
         */
        fun isSuccessful(originalRawContact: ExistingRawContactEntity): Boolean

        /**
         * True if the RawContact with [originalRawContactId] has been successfully moved to the
         * target Account.
         */
        fun isSuccessful(originalRawContactId: Long): Boolean

        /**
         * Returns the ID of the newly created RawContact corresponding to the [originalRawContact].
         * Use the ID to get the newly created RawContact via a query.
         *
         * Returns null if the operation failed.
         */
        fun rawContactId(originalRawContact: ExistingRawContactEntity): Long?

        /**
         * Returns the ID of the newly created RawContact corresponding to the RawContact with the
         * [originalRawContactId]. Use the ID to get the newly created RawContact via a query.
         *
         * Returns null if the operation failed.
         */
        fun rawContactId(originalRawContactId: Long): Long?

        /**
         * Returns the reason why the operation failed for this [originalRawContactId].
         * Null if it did not fail.
         */
        fun failureReason(originalRawContactId: Long): FailureReason?

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result

        enum class FailureReason {

            /**
             * The [Entry.targetAccount] is not in the system.
             */
            INVALID_ACCOUNT,

            /**
             * The RawContact is already associated with the [Entry.targetAccount].
             */
            ALREADY_IN_ACCOUNT,

            /**
             * The RawContact with the given [Entry.rawContactId] may have been deleted prior to
             * this operation or an incorrect RawContact ID was provided.
             */
            RAW_CONTACT_NOT_FOUND,

            /**
             * Inserting a copy of the original RawContact with to the [Entry.targetAccount] failed.
             */
            INSERT_RAW_CONTACT_COPY_FAILED,

            /**
             * A copy of the RawContact was inserted to the target Account but something went
             * wrong with deleting the original RawContact. It may or may not have been deleted.
             *
             * To prevent data loss, this API does NOT attempt to delete the inserted copy in this
             * scenario. This may or may not result in a duplicate RawContact.
             *
             * In this case, the [Result.isSuccessful] will be false even though a new copy of the
             * origin RawContact was inserted. The inserted RawContact will be returned in
             * [Result.rawContactIds] and [Result.rawContactId].
             */
            DELETE_ORIGINAL_RAW_CONTACT_FAILED,

            /**
             * The operation failed because of no permissions, there are no entries,
             * RawContact is from the Profile tables, etc...
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
internal fun MoveRawContactsAcrossAccounts(contacts: Contacts): MoveRawContactsAcrossAccounts =
    MoveRawContactsAcrossAccountsImpl(contacts)

private class MoveRawContactsAcrossAccountsImpl(
    override val contactsApi: Contacts,
    private val entries: MutableSet<Entry> = mutableSetOf(),
    override val isRedacted: Boolean = false
) : MoveRawContactsAcrossAccounts {

    override fun toString(): String =
        """
            MoveRawContactsAcrossAccounts {
                entries: $entries
                hasPermission: ${accountsPermissions.canMoveRawContactsAcrossAccounts()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): MoveRawContactsAcrossAccounts =
        MoveRawContactsAcrossAccountsImpl(
            contactsApi,

            // Redact account info
            entries.redactedCopies().toMutableSet(),

            isRedacted = true
        )

    override fun rawContacts(vararg entries: Entry) = rawContacts(entries.asSequence())

    override fun rawContacts(entries: Collection<Entry>) = rawContacts(entries.asSequence())

    override fun rawContacts(entries: Sequence<Entry>): MoveRawContactsAcrossAccounts = apply {
        this.entries.addAll(entries.redactedCopiesOrThis(isRedacted))
    }

    override fun rawContactsTo(account: Account?, vararg rawContacts: ExistingRawContactEntity) =
        rawContactsTo(account, rawContacts.asSequence())

    override fun rawContactsTo(
        account: Account?,
        rawContacts: Collection<ExistingRawContactEntity>
    ) =
        rawContactsTo(account, rawContacts.asSequence())

    override fun rawContactsTo(
        account: Account?, rawContacts: Sequence<ExistingRawContactEntity>
    ): MoveRawContactsAcrossAccounts = apply {
        rawContacts
            .map { Entry(it.id, account) }
            .also(::rawContacts)
    }

    override fun rawContactsWithIdsTo(account: Account?, vararg rawContactIds: Long) =
        rawContactsWithIdsTo(account, rawContactIds.asSequence())

    override fun rawContactsWithIdsTo(account: Account?, rawContactIds: Collection<Long>) =
        rawContactsWithIdsTo(account, rawContactIds.asSequence())

    override fun rawContactsWithIdsTo(
        account: Account?, rawContactIds: Sequence<Long>
    ): MoveRawContactsAcrossAccounts = apply {
        rawContactIds
            .map { Entry(it, account) }
            .also(::rawContacts)
    }

    override fun commit() = commit { false }

    override fun commit(cancel: () -> Boolean): Result {
        onPreExecute()

        return if (
            entries.isEmpty()
            || !accountsPermissions.canMoveRawContactsAcrossAccounts()
            || cancel()
        ) {
            MoveRawContactsAcrossAccountsResultFailed(FailureReason.UNKNOWN)
        } else {
            val originalToNewRawContacts = mutableMapOf<Long, Long>()
            val failureReasons = mutableMapOf<Long, FailureReason>()

            for (entry in entries) {
                if (cancel()) {
                    break
                }

                // Check if target Account is in system. If it is not in system but is a Samsung
                // phone Account, then it is referencing the local "null" system Account.
                val targetAccount = entry.targetAccount?.nullIfSamsungPhoneAccount()
                if (
                    targetAccount != null
                    && targetAccount.isNotInSystem(contactsApi.applicationContext)
                ) {
                    failureReasons[entry.rawContactId] = FailureReason.INVALID_ACCOUNT
                    break
                }

                // Fetch the original RawContact.
                val originalRawContact: RawContact? = contactsApi
                    .rawContactsQuery()
                    .where { RawContact.Id equalTo entry.rawContactId }
                    .find(cancel)
                    .firstOrNull()

                if (originalRawContact == null) {
                    failureReasons[entry.rawContactId] = FailureReason.RAW_CONTACT_NOT_FOUND
                    break
                }

                // Check if the original and target Accounts are the same.
                if (originalRawContact.account == targetAccount) {
                    failureReasons[entry.rawContactId] = FailureReason.ALREADY_IN_ACCOUNT
                    break
                }

                // Fetch Groups belonging to the target Account that the original RawContact has a
                // membership to based on case-sensitive matching of the Group title.
                val matchedGroupsInTargetAccount = contactsApi
                    .groupsFromTargetAccountMatchingGroupsFromRawContact(
                        originalRawContact, targetAccount, cancel
                    )

                // Insert a copy of the original RawContact.
                val rawContactCopyId = contactsApi.insert().rawContacts()
                    .allowBlanks(true)
                    .rawContacts(
                        originalRawContact.newCopy {
                            // Set the Account.
                            account = entry.targetAccount

                            // Replace the group memberships.
                            // Note that memberships to the target Account's default group and
                            // favorites group are auto added by the Contacts Provider.
                            groupMemberships.clear()
                            groupMemberships.addAll(matchedGroupsInTargetAccount.newMemberships())

                            // Copy over the photo. Note that the Contact photo columns may not
                            // be set immediately after the insert. It is probably done
                            // asynchronously by the Contacts Provider.
                            originalRawContact.photoBytes(contactsApi)?.let { originalPhotoBytes ->
                                setPhoto(PhotoData.from(originalPhotoBytes))
                            }
                        }
                    )
                    .commit(cancel)
                    .rawContactIds
                    .firstOrNull()

                // Check if insert copy failed.
                if (rawContactCopyId == null) {
                    failureReasons[entry.rawContactId] =
                        FailureReason.INSERT_RAW_CONTACT_COPY_FAILED
                    break
                } else {
                    originalToNewRawContacts[originalRawContact.id] = rawContactCopyId
                }

                // Retain links (AggregationExceptions), if any.
                contactsApi.link(rawContactCopyId, originalRawContact.contactId, cancel)

                // Delete the original RawContact.
                val originalDeleted = contentResolver.deleteRawContactsWhere(
                    RawContactsFields.Id equalTo originalRawContact.id
                )

                // Check if delete original failed.
                if (!originalDeleted) {
                    failureReasons[originalRawContact.id] =
                        FailureReason.DELETE_ORIGINAL_RAW_CONTACT_FAILED
                    break
                }
            }

            MoveRawContactsAcrossAccountsResult(originalToNewRawContacts, failureReasons)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

/**
 * If the original RawContact's parent Contact had more than one child RawContact, in other words
 * if the original RawContact had a sibling, then attempt to link the RawContact copy's parent
 * Contact with the original parent Contact. Ignore the result, just attempt it. Google Contacts
 * fails to keep AggregationExceptions in some cases where this function succeeds =)
 *
 * The easiest way to do this is to just link the RawContact copy with the original parent Contact's
 * RawContacts before deleting the original RawContact. This will cover all cases, including the
 * case where there the original RawContact has no sibling.
 */
private fun Contacts.link(
    rawContactCopyId: Long,
    originalRawContactParentId: Long,
    cancel: () -> Boolean
) {
    query()
        .include { Required.all }
        .where {
            /*
             * We cannot use the originalRawContact.id and rawContactCopyId to get the Contacts
             * for linking because the Contacts Provider may have automatically linked them to the
             * same parent Contact (which is expected because the RawContacts are identical) when
             * moving to the same Account. The original RawContact's Contact ID may have been set
             * to the copy's Contact ID in the database. In such cases, the following WHERE clause
             * will result in only one Contact being returned (the copy's new Contact) instead of
             * two (the original and the copy), which will cause the link to fail.
             * RawContact.Id `in` listOf(originalRawContact.id, rawContactCopyId)
             *
             * To fix this, we will use the originalRawContactParentId from memory instead of the
             * one from the database (which may have been changed after he insert operation as
             * discussed above).
             *
             * For example, say that there is a Contact with ID 10 and child RawContacts with IDs
             * 21 and 22 that both belong to Account X...
             *
             * 1. We insert a copy of RawContact 22 to the device (null Account).
             * 2. Contacts Provider creates Contact 11 with child RawContact 23.
             * 3. Contacts Provider determines that RawContact 23 should be linked to (aggregated
             *    with) RawContact 22.
             * 4. Contacts Provider creates Contact 12 with child RawContact 21, 22, and 23 and
             *    deletes Contacts 10 and 11.
             *
             * Before: { Contact 10 { RawContact 21, 22 } }
             * After: { Contact 12 { RawContact 21, 22, 23 } }
             *
             * In this case, we will attempt to link the RawContacts of Contacts 12, which will do
             * nothing because linking requires at least 2 Contacts.
             *
             * Another example is that there is a Contact with ID 10 and child RawContacts with IDs
             * 21 and 22. RawContact 21 belongs to Account X but RawContact 22 belongs to no
             * Account (null Account).
             *
             * 1. We insert a copy of RawContact 22 to Account X.
             * 2. Contacts Provider creates Contact 11 with child RawContact 23.
             * 3. Contacts Provider determines that RawContact 22 should be linked to (aggregated
             *    with) RawContact 23 instead of RawContact 21.
             * 4. Contacts Provider updates the AggregationExceptions table such that RawContact
             *    21 no longer has a sibling.
             *
             * Before: { Contact 10 { RawContact 21, 22 } }
             * After: { Contact 10 { RawContact 21 } }, { Contact 11 { RawContact 22, 23 } }
             *
             * In this case, we will attempt to link the RawContacts of Contacts 10 and 11.
             *
             * To reiterate, the following WHERE clause,
             * RawContact.Id `in` listOf(originalRawContact.id, rawContactCopyId)
             * will result in only Contact 11 to be returned as originalRawContact.id (22) and
             * rawContactCopyId (23) will only match Contact 11.
             */
            (RawContact.Id equalTo rawContactCopyId) or (Contact.Id equalTo originalRawContactParentId)
        }
        .find(cancel)
        .linkDirect(this)
}

private fun Contacts.groupsFromTargetAccountMatchingGroupsFromRawContact(
    rawContact: RawContact, targetAccount: Account?, cancel: () -> Boolean
): List<Group> {
    val rawContactGroupIds = rawContact.groupMemberships.mapNotNull { it.groupId }

    if (rawContactGroupIds.isEmpty()) {
        return emptyList()
    }

    val rawContactGroups = groups()
        .query()
        .where { Id `in` rawContactGroupIds }
        .find(cancel)

    if (rawContactGroups.isEmpty()) {
        return emptyList()
    }

    return groups()
        .query()
        .accounts(targetAccount)
        .where { Title `in` rawContactGroups.map { it.title } }
        .find(cancel)
}

private class MoveRawContactsAcrossAccountsResult private constructor(
    /**
     * Map of [Entry.rawContactId] to the corresponding newly created/inserted RawContact ID, which
     * is null of the operation failed.
     */
    private val originalToNewRawContacts: Map<Long, Long>,

    /**
     * Map of [Entry.rawContactId] to a [FailureReason], which is null if operation succeeded.
     */
    private val failureReasons: Map<Long, FailureReason>,

    override val isRedacted: Boolean
) : Result {

    constructor(
        originalToNewRawContacts: Map<Long, Long>,
        failureReasons: Map<Long, FailureReason>
    ) : this(
        originalToNewRawContacts = originalToNewRawContacts,
        failureReasons = failureReasons,
        isRedacted = false
    )

    override fun toString(): String =
        """
            MoveRawContactsAcrossAccounts.Result {
                isSuccessful: $isSuccessful
                originalToNewRawContacts: $originalToNewRawContacts
                failureReasons: $failureReasons
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Result = MoveRawContactsAcrossAccountsResult(
        originalToNewRawContacts = originalToNewRawContacts,
        failureReasons = failureReasons,
        isRedacted = true
    )

    override val isSuccessful: Boolean by lazy { failureReasons.isEmpty() }

    override val rawContactIds: List<Long> = originalToNewRawContacts.values.toList()

    override fun isSuccessful(originalRawContact: ExistingRawContactEntity): Boolean =
        isSuccessful(originalRawContact.id)

    override fun isSuccessful(originalRawContactId: Long): Boolean =
        failureReason(originalRawContactId) == null

    override fun rawContactId(originalRawContact: ExistingRawContactEntity): Long? =
        rawContactId(originalRawContact.id)

    override fun rawContactId(originalRawContactId: Long): Long? =
        originalToNewRawContacts[originalRawContactId]

    override fun failureReason(originalRawContactId: Long): FailureReason? =
        failureReasons[originalRawContactId]
}

private class MoveRawContactsAcrossAccountsResultFailed private constructor(
    private val failureReason: FailureReason,
    override val isRedacted: Boolean
) : Result {

    constructor(failureReason: FailureReason) : this(
        failureReason = failureReason,
        isRedacted = false
    )

    override fun toString(): String =
        """
            MoveRawContactsAcrossAccounts.Result {
                isSuccessful: $isSuccessful
                failureReason: $failureReason
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Result = MoveRawContactsAcrossAccountsResultFailed(
        failureReason = failureReason,
        isRedacted = true
    )

    override val isSuccessful: Boolean = false

    override val rawContactIds: List<Long> = emptyList()

    override fun isSuccessful(originalRawContact: ExistingRawContactEntity): Boolean = isSuccessful

    override fun isSuccessful(originalRawContactId: Long): Boolean = isSuccessful

    override fun rawContactId(originalRawContact: ExistingRawContactEntity): Long? = null

    override fun rawContactId(originalRawContactId: Long): Long? = null

    override fun failureReason(originalRawContactId: Long) = failureReason
}