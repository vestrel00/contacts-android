package contacts.groups

import android.content.ContentResolver
import android.content.Context
import contacts.ContactsPermissions
import contacts.entities.MutableGroup
import contacts.entities.operation.GroupsOperation
import contacts.util.applyBatch
import contacts.util.unsafeLazy

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
        fun isSuccessful(group: MutableGroup): Boolean
    }
}

@Suppress("FunctionName")
internal fun GroupsUpdate(context: Context): GroupsUpdate = GroupsUpdateImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class GroupsUpdateImpl(
    private val contentResolver: ContentResolver,
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

        val results = mutableMapOf<Long, Boolean>()
        for (group in groups) {
            if (cancel()) {
                break
            }

            if (group?.id != null) {
                results[group.id] = if (group.readOnly) {
                    false
                } else {
                    contentResolver.updateGroup(group)
                }
            } else {
                results[INVALID_ID] = false
            }
        }
        return GroupsUpdateResult(results)
    }

    private companion object {
        // A failed entry in the results so that Result.isSuccessful returns false.
        const val INVALID_ID = -1L
    }
}

private fun ContentResolver.updateGroup(group: MutableGroup): Boolean =
    GroupsOperation().update(group)?.let { applyBatch(it) } != null

private class GroupsUpdateResult(private val groupIdsResultMap: Map<Long, Boolean>) :
    GroupsUpdate.Result {

    override val isSuccessful: Boolean by unsafeLazy { groupIdsResultMap.all { it.value } }

    override fun isSuccessful(group: MutableGroup): Boolean = group.id != null
            && groupIdsResultMap.getOrElse(group.id) { false }
}

private class GroupsUpdateFailed : GroupsUpdate.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: MutableGroup): Boolean = false
}
