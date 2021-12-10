package contacts.core.groups

import android.accounts.Account
import android.content.ContentResolver
import contacts.core.Contacts
import contacts.core.ContactsPermissions
import contacts.core.accounts.AccountsQuery
import contacts.core.entities.NewGroup
import contacts.core.entities.operation.GroupsOperation
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
 * ## Accounts
 *
 * A set of groups exist for each [Account]. When there are no accounts in the system, there are
 * no groups and inserting groups will fail.
 *
 * The get accounts permission is required here because this API retrieves all available accounts,
 * if any, and does the following;
 *
 * - if the account specified is found in the list of accounts returned by the system, the account
 * is used
 * - if the account specified is not found in the list of accounts returned by the system, then the
 * insertion fails for that group
 * - if there are no accounts in the system, [commit] does nothing and fails immediately
 *
 * ## Usage
 *
 * To insert a group with the title "Best Friends" for the given account;
 *
 * ```kotlin
 * val result = groupsInsert
 *      .groups(NewGroup("Best Friends", account))
 *      .commit()
 * ```
 */
interface GroupsInsert {

    /**
     * Adds a new [NewGroup] to the insert queue, which will be inserted on [commit].
     * The new instance is created with the given [title] and [account].
     */
    fun group(title: String, account: Account): GroupsInsert

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
     * Groups with titles that already exist will be inserted. The Contacts Provider allows this
     * and is the behavior of the native Contacts app. If desired, it is up to consumers to protect
     * against multiple groups from the same account having the same titles.
     *
     * This does nothing if there are no available accounts or no groups are in the insert queue or
     * if insert permission has not been granted. An empty map will be returned in this case.
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
     * Groups with titles that already exist will be inserted. The Contacts Provider allows this
     * and is the behavior of the native Contacts app. If desired, it is up to consumers to protect
     * against multiple groups from the same account having the same titles.
     *
     * This does nothing if there are no available accounts or no groups are in the insert queue or
     * if insert permission has not been granted. An empty map will be returned in this case.
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

    interface Result {

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
         * a query. The manually constructed [NewGroup] passed to [GroupsInsert.groups] are not
         * automatically updated and will remain to have an invalid ID.
         *
         * Returns null if the insert operation failed.
         */
        fun groupId(group: NewGroup): Long?

        /**
         * Returns the reason why the insert failed for this [group]. Null if it did not fail.
         */
        fun failureReason(group: NewGroup): FailureReason?

        enum class FailureReason {

            /**
             * The Contacts Provider allows multiple groups with the same title (case-sensitive
             * comparison) belonging to the same account to exist. In older versions of Android,
             * the native Contacts app allows the creation of new groups with existing titles. In
             * newer versions, duplicate titles are not allowed. Therefore, this library does not
             * allow for duplicate titles.
             *
             * In newer versions, the group with the duplicate title gets deleted either
             * automatically by the Contacts Provider or when viewing groups in the native Contacts
             * app. It's not an immediate failure on insert or update. This could lead to bugs!
             */
            TITLE_ALREADY_EXIST,

            /**
             * The Group's Account is not found in the system.
             */
            INVALID_ACCOUNT,

            /**
             * The update failed because of no permissions, no groups specified for update, etc...
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
internal fun GroupsInsert(contacts: Contacts): GroupsInsert = GroupsInsertImpl(
    contacts.applicationContext.contentResolver,
    contacts.accounts().query(),
    contacts.groups().query(),
    contacts.permissions
)

private class GroupsInsertImpl(
    private val contentResolver: ContentResolver,
    private val accountsQuery: AccountsQuery,
    private val groupsQuery: GroupsQuery,
    private val permissions: ContactsPermissions,
    private val groups: MutableSet<NewGroup> = mutableSetOf()
) : GroupsInsert {

    override fun toString(): String =
        """
            GroupsInsert {
                groups: $groups
            }
        """.trimIndent()

    override fun group(title: String, account: Account): GroupsInsert =
        groups(NewGroup(title, account))

    override fun groups(vararg groups: NewGroup) = groups(groups.asSequence())

    override fun groups(groups: Collection<NewGroup>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<NewGroup>): GroupsInsert = apply {
        this.groups.addAll(groups)
    }

    override fun commit(): GroupsInsert.Result = commit { false }

    override fun commit(cancel: () -> Boolean): GroupsInsert.Result {
        if (groups.isEmpty() || !permissions.canInsert() || cancel()) {
            return GroupsInsertFailed()
        }

        val accounts = accountsQuery.allAccounts()
        if (accounts.isEmpty()) {
            // Fail if there are no accounts. A group requires Accounts in the system to exist!
            return GroupsInsertFailed()
        }

        // Gather the accounts for groups that will be inserted.
        val groupsAccounts = groups.map { it.account }

        // Gather the existing titles per account to prevent duplicates.
        val existingGroups = groupsQuery
            // Limit the accounts for optimization in case there are a lot of accounts in the system
            .accounts(groupsAccounts)
            .find()
        val existingAccountGroupsTitles = mutableMapOf<Account, MutableSet<String>>()
        for (group in existingGroups) {
            val existingTitles = existingAccountGroupsTitles
                .getOrPut(group.account) { mutableSetOf() }
            existingTitles.add(group.title)
        }

        val results = mutableMapOf<NewGroup, Long?>()
        val failureReasons = mutableMapOf<NewGroup, GroupsInsert.Result.FailureReason>()

        for (group in groups) {
            if (cancel()) {
                break
            }

            results[group] = if (accounts.contains(group.account)) { // Group has a valid account.
                val existingTitles = existingAccountGroupsTitles
                    .getOrPut(group.account) { mutableSetOf() }
                if (existingTitles.contains(group.title)) { // Group title already exist.
                    failureReasons[group] = GroupsInsert.Result.FailureReason.TITLE_ALREADY_EXIST
                    null
                } else { // Group title does not yet exist. Proceed to insert.
                    contentResolver.insertGroup(group).also { id ->
                        if (id == null) { // Insert failed.
                            failureReasons[group] = GroupsInsert.Result.FailureReason.UNKNOWN
                        } else { // Insert succeeded. Add title to existing titles list.
                            existingTitles.add(group.title)
                        }
                    }
                }
            } else { // Group has an invalid account.
                failureReasons[group] = GroupsInsert.Result.FailureReason.INVALID_ACCOUNT
                null
            }
        }

        return GroupsInsertResult(results, failureReasons)
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

private class GroupsInsertResult(
    private val groupsMap: Map<NewGroup, Long?>,
    private val failureReasons: Map<NewGroup, GroupsInsert.Result.FailureReason>
) : GroupsInsert.Result {

    override val groupIds: List<Long> by unsafeLazy {
        groupsMap.asSequence()
            .mapNotNull { it.value }
            .toList()
    }

    override val isSuccessful: Boolean by unsafeLazy { groupsMap.all { it.value != null } }

    override fun isSuccessful(group: NewGroup): Boolean = groupId(group) != null

    override fun groupId(group: NewGroup): Long? = groupsMap.getOrElse(group) { null }

    override fun failureReason(group: NewGroup) = failureReasons[group]
}

private class GroupsInsertFailed : GroupsInsert.Result {

    override val groupIds: List<Long> = emptyList()

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: NewGroup): Boolean = false

    override fun groupId(group: NewGroup): Long? = null

    override fun failureReason(group: NewGroup) = GroupsInsert.Result.FailureReason.UNKNOWN
}