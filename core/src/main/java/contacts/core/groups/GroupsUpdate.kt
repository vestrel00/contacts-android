package contacts.core.groups

import android.accounts.Account
import android.content.ContentResolver
import contacts.core.Contacts
import contacts.core.ContactsPermissions
import contacts.core.entities.MutableGroup
import contacts.core.entities.operation.GroupsOperation
import contacts.core.util.applyBatch
import contacts.core.util.unsafeLazy

/**
 * Updates one or more groups rows in the groups table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All updates will do nothing if these  permissions are not granted.
 *
 * ## Usage
 *
 * To update a groups's name to "Best Friends";
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = groupsUpdate
 *      .groups(group.toMutableGroup()?.apply {
 *          title = "Best Friends"
 *      })
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * MutableGroup mutableGroup = group.toMutableGroup();
 *
 * if (mutableGroup != null) {
 *   mutableGroup.setTitle("Best Friends");
 *
 *   GroupsUpdate.Result result = groupsUpdate
 *        .groups(mutableGroup)
 *        .commit();
 * }
 * ```
 */
interface GroupsUpdate {

    /**
     * Adds the given [groups] to the update queue, which will be updated on [commit].
     *
     * Only existing [groups] that have been retrieved via a query will be added to the update
     * queue. Those that have been manually created via a constructor will be ignored and result
     * in a failed operation.
     *
     * ## Null [MutableGroup]s
     *
     * Null groups are ignored and result in a failed operation. The only reason null is allowed to
     * be passed here is for consumer convenience because the group's `toMutable` returns null if
     * the `readOnly` property is true.
     *
     * ## Read-only [MutableGroup]s
     *
     * Read-only groups will be ignored and result in a failed operation.
     */
    fun groups(vararg groups: MutableGroup?): GroupsUpdate

    /**
     * See [GroupsUpdate.groups].
     */
    fun groups(groups: Collection<MutableGroup?>): GroupsUpdate

    /**
     * See [GroupsUpdate.groups].
     */
    fun groups(groups: Sequence<MutableGroup?>): GroupsUpdate

    /**
     * Updates the [MutableGroup]s in the queue (added via [groups]) and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Updates the [MutableGroup]s in the queue (added via [groups]) and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo updates. This means that depending on when the cancellation
     * occurs, some if not all of the Groups in the update queue may have already been updated.**
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
         * True if all Groups have successfully been updated. False if even one update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [group] has been successfully updated. False otherwise.
         */
        fun isSuccessful(group: MutableGroup?): Boolean

        /**
         * Returns the reason why the insert failed for this [group]. Null if it did not fail.
         */
        fun failureReason(group: MutableGroup?): FailureReason?

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
             * The update failed because of no permissions, no groups specified for update, group
             * is read-only, etc...
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
internal fun GroupsUpdate(contacts: Contacts): GroupsUpdate = GroupsUpdateImpl(
    contacts.applicationContext.contentResolver,
    contacts.groups().query(),
    contacts.permissions
)

private class GroupsUpdateImpl(
    private val contentResolver: ContentResolver,
    private val groupsQuery: GroupsQuery,
    private val permissions: ContactsPermissions,
    private val groups: MutableSet<MutableGroup?> = mutableSetOf()
) : GroupsUpdate {

    override fun toString(): String =
        """
            GroupsUpdate {
                groups: $groups
            }
        """.trimIndent()

    override fun groups(vararg groups: MutableGroup?) = groups(groups.asSequence())

    override fun groups(groups: Collection<MutableGroup?>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<MutableGroup?>): GroupsUpdate = apply {
        this.groups.addAll(groups)
    }

    override fun commit(): GroupsUpdate.Result = commit { false }

    override fun commit(cancel: () -> Boolean): GroupsUpdate.Result {
        if (groups.isEmpty() || !permissions.canUpdateDelete() || cancel()) {
            return GroupsUpdateFailed()
        }

        // Gather the accounts for groups that will be updated.
        val groupsAccounts = groups.mapNotNull { it?.account }

        // Gather the existing groups per account to prevent duplicate titles.
        val existingGroups = groupsQuery
            // Limit the accounts for optimization in case there are a lot of accounts in the system
            .accounts(groupsAccounts)
            .find()
            // Convert to mutable group so that titles can be mutated during update processing.
            // Use the data class copy function intentionally to include read-only groups.
            .map { it.copy(readOnly = false).toMutableGroup()!! } //  Consumers should never do this!!!
        val existingAccountGroups = mutableMapOf<Account, MutableSet<MutableGroup>>()
        for (group in existingGroups) {
            existingAccountGroups.getOrPut(group.account) { mutableSetOf() }.also {
                it.add(group)
            }
        }

        val failureReasons = mutableMapOf<MutableGroup?, GroupsUpdate.Result.FailureReason>()

        for (group in groups) {
            if (cancel()) {
                break
            }

            if (group?.id != null) { // Make sure the ID is not null to ensure the group exists.
                val accountGroups = existingAccountGroups
                    .getOrPut(group.account) { mutableSetOf(group) }

                val differentGroupWithSameTitle = accountGroups
                    .find { it.title == group.title && it.id != group.id }

                if (differentGroupWithSameTitle != null) {
                    // The title of this group belongs to a different existing group.
                    failureReasons[group] = GroupsUpdate.Result.FailureReason.TITLE_ALREADY_EXIST
                } else if (contentResolver.updateGroup(group)) {
                    /*
                     * Update success.
                     *
                     * We also need to update the title in our temporarily list to ensure that the
                     * next iteration of this for-loop has the updated set of titles. For example,
                     *
                     * 1. there are groups [A, B, C]
                     * 2. update C -> D: [A, B, D]
                     * 3. update B -> C: [A, C, D]
                     *
                     * If we did not update our list from [A, B, C] to [A, B, D],
                     * the update for B -> C will fail with TITLE_ALREADY_EXIST
                     */
                    val groupInMemory = accountGroups.find { it.id == group.id }
                    groupInMemory?.title = group.title
                } else {
                    // Update failed.
                    failureReasons[group] = GroupsUpdate.Result.FailureReason.UNKNOWN
                }
            } else {
                failureReasons[group] = GroupsUpdate.Result.FailureReason.UNKNOWN
            }
        }

        return GroupsUpdateResult(failureReasons)
    }
}

private fun ContentResolver.updateGroup(group: MutableGroup): Boolean =
    GroupsOperation().update(group)?.let { applyBatch(it) } != null

private class GroupsUpdateResult(
    private val failureReasons: Map<MutableGroup?, GroupsUpdate.Result.FailureReason>
) : GroupsUpdate.Result {

    override val isSuccessful: Boolean by unsafeLazy { failureReasons.isEmpty() }

    override fun isSuccessful(group: MutableGroup?): Boolean = failureReason(group) == null

    override fun failureReason(group: MutableGroup?): GroupsUpdate.Result.FailureReason? =
        failureReasons[group]
}

private class GroupsUpdateFailed : GroupsUpdate.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: MutableGroup?): Boolean = false

    override fun failureReason(group: MutableGroup?) = GroupsUpdate.Result.FailureReason.UNKNOWN
}
