package contacts.core.groups

import android.accounts.Account
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.NewGroup
import contacts.core.entities.operation.GroupsOperation
import contacts.core.groups.GroupsInsert.Result.FailureReason
import contacts.core.util.applyBatch
import contacts.core.util.unsafeLazy

/**
 * Inserts one or more user groups into the groups table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] and
 * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are assumed to have been
 * granted already in these examples for brevity. All inserts will do nothing if these permissions
 * are not granted.
 *
 * ## Usage
 *
 * To insert a group with the title "Best Friends" for the given (nullable) account,
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = groupsInsert.group("Best Friends", account).commit()
 * ```
 *
 * In Java,
 *
 * ```kotlin
 * GroupsInsert.Result result = groupsInsert.groups("Best Friends", account).commit();
 * ```
 */
interface GroupsInsert : CrudApi {

    /**
     * Adds a new [NewGroup] to the insert queue, which will be inserted on [commit].
     * The new instance is created with the given [title] and [account].
     */
    fun group(title: String, account: Account?): GroupsInsert

    /**
     * Adds the given [groups] to the insert queue, which will be inserted on [commit].
     * Duplicates (groups with identical attributes to already added groups) are ignored.
     */
    fun groups(vararg groups: NewGroup): GroupsInsert

    /**
     * See [GroupsInsert.groups].
     */
    fun groups(groups: Collection<NewGroup>): GroupsInsert

    /**
     * See [GroupsInsert.groups].
     */
    fun groups(groups: Sequence<NewGroup>): GroupsInsert

    /**
     * Inserts the [NewGroup]s in the queue (added via [groups]) and returns the [Result].
     *
     * ## Permissions
     *
     * The [ContactsPermissions.WRITE_PERMISSION] and
     * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Inserts the [NewGroup]s in the queue (added via [groups]) and returns the [Result].
     *
     * ## Permissions
     *
     * The [ContactsPermissions.WRITE_PERMISSION] and
     * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo insertions. This means that depending on when the cancellation
     * occurs, some if not all of the Groups in the insert queue may have already been inserted.**
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
    override fun redactedCopy(): GroupsInsert

    interface Result : CrudApi.Result {

        /**
         * The list of IDs of successfully created Groups.
         */
        val groupIds: List<Long>

        /**
         * True if all NewGroups have successfully been inserted. False if even one insert
         * failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [group] has been successfully inserted. False otherwise.
         */
        fun isSuccessful(group: NewGroup): Boolean

        /**
         * Returns the ID of the newly created Group. Use the ID to get the newly created Group via
         * a query.
         *
         * Returns null if the insert operation failed.
         */
        fun groupId(group: NewGroup): Long?

        /**
         * Returns the reason why the insert failed for this [group]. Null if it did not fail.
         */
        fun failureReason(group: NewGroup): FailureReason?

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result

        enum class FailureReason {

            /**
             * The Contacts Provider allows multiple groups with the same title (case-sensitive
             * comparison) belonging to the same account to exist. In older versions of Android,
             * the AOSP Contacts app allows the creation of new groups with existing titles. In
             * newer versions, duplicate titles are not allowed. Therefore, this library does not
             * allow for duplicate titles.
             *
             * In newer versions, the group with the duplicate title gets deleted either
             * automatically by the Contacts Provider or when viewing groups in the AOSP Contacts
             * app. It's not an immediate failure on insert or update. This could lead to bugs!
             */
            TITLE_ALREADY_EXIST,

            /**
             * The Group's Account is not found in the system.
             *
             * A null account is a valid "account". On the other hand, a non-null account that is
             * not in the system AccountManager is invalid.
             */
            INVALID_ACCOUNT,

            /**
             * The update failed because of no permissions or no groups specified for update, etc...
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
internal fun GroupsInsert(contacts: Contacts): GroupsInsert = GroupsInsertImpl(contacts)

private class GroupsInsertImpl(
    override val contactsApi: Contacts,

    private val groups: MutableSet<NewGroup> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : GroupsInsert {

    override fun toString(): String =
        """
            GroupsInsert {
                groups: $groups
                hasPermission: ${permissions.canInsert()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsInsert = GroupsInsertImpl(
        contactsApi,

        // Redact group data.
        groups.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun group(title: String, account: Account?): GroupsInsert =
        groups(NewGroup(title, account))

    override fun groups(vararg groups: NewGroup) = groups(groups.asSequence())

    override fun groups(groups: Collection<NewGroup>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<NewGroup>): GroupsInsert = apply {
        this.groups.addAll(groups.redactedCopiesOrThis(isRedacted))
    }

    override fun commit(): GroupsInsert.Result = commit { false }

    override fun commit(cancel: () -> Boolean): GroupsInsert.Result {
        onPreExecute()

        val accounts = contactsApi.accounts().query().find()
        return if (groups.isEmpty() || !permissions.canInsert() || cancel()) {
            GroupsInsertFailed()
        } else {
            // Gather the accounts for groups that will be inserted.
            val groupsAccounts = groups.asSequence().map { it.account }.toSet()

            // Gather the existing titles per account to prevent duplicates.
            val existingGroups = contactsApi.groups().query().accounts(groupsAccounts).find()
            val existingAccountGroupsTitles = mutableMapOf<Account?, MutableSet<String>>()
            for (group in existingGroups) {
                val existingTitles = existingAccountGroupsTitles
                    .getOrPut(group.account) { mutableSetOf() }
                existingTitles.add(group.title)
            }

            val results = mutableMapOf<NewGroup, Long?>()
            val failureReasons = mutableMapOf<NewGroup, FailureReason>()

            for (group in groups) {
                if (cancel()) {
                    break
                }

                results[group] = if (group.account == null || accounts.contains(group.account)) {
                    // Group has a valid account. A null account is valid.
                    val existingTitles = existingAccountGroupsTitles
                        .getOrPut(group.account) { mutableSetOf() }
                    if (existingTitles.contains(group.title)) {
                        // Group title already exist.
                        failureReasons[group] = FailureReason.TITLE_ALREADY_EXIST
                        null
                    } else {
                        // Group title does not yet exist. Proceed to insert.
                        contentResolver.insertGroup(group).also { id ->
                            if (id == null) {
                                // Insert failed.
                                failureReasons[group] = FailureReason.UNKNOWN
                            } else {
                                // Insert succeeded. Add title to existing titles list.
                                existingTitles.add(group.title)
                            }
                        }
                    }
                } else {
                    // Group has an invalid account.
                    failureReasons[group] = FailureReason.INVALID_ACCOUNT
                    null
                }
            }

            GroupsInsertResult(results, failureReasons)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private fun ContentResolver.insertGroup(group: NewGroup): Long? {
    val results = applyBatch(GroupsOperation().insert(group))

    /*
     * The ContentProviderResult[0] contains the first result of the batch, which is the
     * GroupOperation. The uri contains the Groups._ID as the last path segment.
     *
     * E.G. "content://com.android.contacts/groups/18"
     * In this case, 18 is the Groups._ID.
     *
     * It is formed by the Contacts Provider using
     * Uri.withAppendedPath(ContactsContract.Groups.CONTENT_URI, "18")
     */
    return results?.firstOrNull()?.let { result ->
        val groupUri = result.uri
        val groupId = groupUri?.lastPathSegment?.toLongOrNull()
        groupId
    }
}

private class GroupsInsertResult private constructor(
    private val groupsMap: Map<NewGroup, Long?>,
    private val failureReasons: Map<NewGroup, FailureReason>,
    override val isRedacted: Boolean
) : GroupsInsert.Result {

    constructor(
        groupsMap: Map<NewGroup, Long?>,
        failureReasons: Map<NewGroup, FailureReason>
    ) : this(groupsMap, failureReasons, false)

    override fun toString(): String =
        """
            GroupsInsert.Result {
                isSuccessful: $isSuccessful
                groupsMap: $groupsMap
                failureReasons: $failureReasons
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsInsert.Result = GroupsInsertResult(
        groupsMap.redactedKeys(),
        failureReasons.redactedKeys(),
        isRedacted = true
    )

    override val groupIds: List<Long> by unsafeLazy {
        groupsMap.asSequence()
            .mapNotNull { it.value }
            .toList()
    }

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        groupsMap.run { isNotEmpty() && all { it.value != null } }
    }

    override fun isSuccessful(group: NewGroup): Boolean = groupId(group) != null

    override fun groupId(group: NewGroup): Long? = groupsMap.getOrElse(group) { null }

    override fun failureReason(group: NewGroup): FailureReason? = failureReasons[group]
}

private class GroupsInsertFailed private constructor(override val isRedacted: Boolean) :
    GroupsInsert.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            GroupsInsert.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsInsert.Result = GroupsInsertFailed(true)

    override val groupIds: List<Long> = emptyList()

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: NewGroup): Boolean = false

    override fun groupId(group: NewGroup): Long? = null

    override fun failureReason(group: NewGroup) = FailureReason.UNKNOWN
}