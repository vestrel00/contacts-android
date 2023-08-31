package contacts.core.groups

import android.accounts.Account
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.ExistingGroupEntity
import contacts.core.entities.Group
import contacts.core.entities.MutableGroup
import contacts.core.entities.operation.GroupsOperation
import contacts.core.groups.GroupsUpdate.Result.FailureReason
import contacts.core.util.applyBatch

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
 * To update a groups' name to "Best Friends",
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = groupsUpdate
 *      .groups(group.mutableCopy().apply {
 *          title = "Best Friends"
 *      })
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * MutableGroup mutableGroup = group.mutableCopy();
 *
 * mutableGroup.setTitle("Best Friends");
 *
 * GroupsUpdate.Result result = groupsUpdate
 *      .groups(mutableGroup)
 *      .commit();
 * ```
 */
interface GroupsUpdate : CrudApi {

    /**
     * Adds the given [groups] to the update queue, which will be updated on [commit].
     *
     * ## Read-only [ExistingGroupEntity]s
     *
     * Read-only groups will be ignored and result in a failed operation.
     */
    fun groups(vararg groups: ExistingGroupEntity): GroupsUpdate

    /**
     * See [GroupsUpdate.groups].
     */
    fun groups(groups: Collection<ExistingGroupEntity>): GroupsUpdate

    /**
     * See [GroupsUpdate.groups].
     */
    fun groups(groups: Sequence<ExistingGroupEntity>): GroupsUpdate

    /**
     * Updates the [ExistingGroupEntity]s in the queue (added via [groups]) and returns the [Result].
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
     * Updates the [ExistingGroupEntity]s in the queue (added via [groups]) and returns the [Result].
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
    override fun redactedCopy(): GroupsUpdate

    interface Result : CrudApi.Result {

        /**
         * True if all Groups have successfully been updated. False if even one update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [group] has been successfully updated. False otherwise.
         */
        fun isSuccessful(group: ExistingGroupEntity): Boolean

        /**
         * Returns the reason why the insert failed for this [group]. Null if it did not fail.
         */
        fun failureReason(group: ExistingGroupEntity): FailureReason?

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
internal fun GroupsUpdate(contacts: Contacts): GroupsUpdate = GroupsUpdateImpl(contacts)

private class GroupsUpdateImpl(
    override val contactsApi: Contacts,

    private val groups: MutableSet<ExistingGroupEntity> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : GroupsUpdate {

    override fun toString(): String =
        """
            GroupsUpdate {
                groups: $groups
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsUpdate = GroupsUpdateImpl(
        contactsApi,

        // Redact group info.
        groups.asSequence().map { it.redactedCopy() }.toMutableSet(),

        isRedacted = true
    )

    override fun groups(vararg groups: ExistingGroupEntity) = groups(groups.asSequence())

    override fun groups(groups: Collection<ExistingGroupEntity>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<ExistingGroupEntity>): GroupsUpdate = apply {
        this.groups.addAll(groups.map { it.redactedCopyOrThis(isRedacted) })
    }

    override fun commit(): GroupsUpdate.Result = commit { false }

    override fun commit(cancel: () -> Boolean): GroupsUpdate.Result {
        onPreExecute()

        return if (groups.isEmpty() || !permissions.canUpdateDelete() || cancel()) {
            GroupsUpdateFailed()
        } else {
            // Gather the accounts for groups that will be updated.
            val groupsAccounts = groups.map { it.account }

            // Gather the existing groups per account to prevent duplicate titles.
            val existingGroups = contactsApi.groups().query().accounts(groupsAccounts).find()
            val existingAccountGroups = mutableMapOf<Account?, MutableSet<ExistingGroupEntity>>()
            for (group in existingGroups) {
                existingAccountGroups.getOrPut(group.account) { mutableSetOf() }.also {
                    it.add(group)
                }
            }

            val failureReasons = mutableMapOf<ExistingGroupEntity, FailureReason>()

            for (group in groups) {
                // Intentionally not breaking if cancelled so that all groups are assigned a failure
                // reason. Unlike other APIs in this library, this API will indicate success if there
                // is no failure reason.

                if (!cancel()) {
                    val accountGroups = existingAccountGroups
                        .getOrPut(group.account) { mutableSetOf(group) }

                    val differentGroupWithSameTitle = accountGroups
                        .find { it.title == group.title && it.id != group.id }

                    if (differentGroupWithSameTitle != null) {
                        // The title of this group belongs to a different existing group.
                        failureReasons[group] = FailureReason.TITLE_ALREADY_EXIST
                    } else if (!cancel() && contentResolver.updateGroup(group)) {
                        /*
                         * Update success.
                         *
                         * We also need to update the title in our temporary list to ensure that the
                         * next iteration of this for-loop has the updated set of titles. For example,
                         *
                         * 1. there are groups [A, B, C]
                         * 2. update C -> D: [A, B, D]
                         * 3. update B -> C: [A, C, D]
                         *
                         * If we did not update our list from [A, B, C] to [A, B, D],
                         * the update for B -> C will fail with TITLE_ALREADY_EXIST
                         */
                        accountGroups.find { it.id == group.id }?.let { groupInMemory ->
                            when (groupInMemory) {
                                is MutableGroup -> groupInMemory.title = group.title
                                is Group -> {
                                    // We have to replace because this is immutable.
                                    accountGroups.remove(groupInMemory)
                                    accountGroups.add(groupInMemory.copy(title = group.title))
                                }
                            }
                        }
                    } else {
                        failureReasons[group] = FailureReason.UNKNOWN
                    }
                } else {
                    failureReasons[group] = FailureReason.UNKNOWN
                }
            }

            GroupsUpdateResult(failureReasons)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private fun ContentResolver.updateGroup(group: ExistingGroupEntity): Boolean =
    applyBatch(GroupsOperation().update(group)) != null

private class GroupsUpdateResult private constructor(
    private val failureReasons: Map<ExistingGroupEntity, FailureReason>,
    override val isRedacted: Boolean = false
) : GroupsUpdate.Result {

    constructor(failureReasons: Map<ExistingGroupEntity, FailureReason>) : this(
        failureReasons,
        false
    )

    override fun toString(): String =
        """
            GroupsUpdate.Result {
                isSuccessful: $isSuccessful
                failureReasons: $failureReasons
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsUpdate.Result = GroupsUpdateResult(
        // Redact group data.
        failureReasons.mapKeys { it.key.redactedCopy() },
        isRedacted = true
    )

    override val isSuccessful: Boolean by lazy { failureReasons.isEmpty() }

    override fun isSuccessful(group: ExistingGroupEntity): Boolean = failureReason(group) == null

    override fun failureReason(group: ExistingGroupEntity): FailureReason? =
        failureReasons[group]
}

private class GroupsUpdateFailed(override val isRedacted: Boolean = false) : GroupsUpdate.Result {

    constructor() : this(false)


    override fun toString(): String =
        """
            GroupsUpdate.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsUpdate.Result = GroupsUpdateFailed(true)

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: ExistingGroupEntity): Boolean = false

    override fun failureReason(group: ExistingGroupEntity) = FailureReason.UNKNOWN
}
