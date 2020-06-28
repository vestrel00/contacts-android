package com.vestrel00.contacts.groups

import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.operation.GroupOperation
import com.vestrel00.contacts.util.applyBatch

/**
 * Deletes one or more groups from the groups table.
 *
 * FIXME? Expose this to consumers? For more details, see the DEV_NOTES "Groups; Deletion" section.
 * Marked as internal until it can be exposed to consumers (if ever).
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All deletes will do nothing if the permission is not granted.
 *
 * ## Usage
 *
 * To delete the given groups;
 *
 * ```kotlin
 * groupsDelete
 *      .groups(groups)
 *      .commit()
 * ```
 */
internal interface GroupsDelete {

    /**
     * Adds the given [groups] to the delete queue, which will be deleted on [commit].
     *
     * Read-only groups will be ignored and result in a failed operation.
     */
    fun groups(vararg groups: Group): GroupsDelete

    /**
     * See [GroupsDelete.groups].
     */
    fun groups(groups: Collection<Group>): GroupsDelete

    /**
     * See [GroupsDelete.groups].
     */
    fun groups(groups: Sequence<Group>): GroupsDelete

    /**
     * Deletes the [Group]s in the queue (added via [groups]) and returns the [Result].
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
     * Deletes the [Group]s in the queue (added via [groups]) in one transaction. Either ALL deletes
     * succeed or ALL fail.
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
    fun commitInOneTransaction(): Boolean

    interface Result {

        /**
         * True if all Groups have successfully been deleted. False if even one delete failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [group] has been successfully deleted. False otherwise.
         */
        fun isSuccessful(group: Group): Boolean
    }
}

@Suppress("FunctionName")
internal fun GroupsDelete(context: Context): GroupsDelete = GroupsDeleteImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class GroupsDeleteImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val groupIds: MutableSet<Long> = mutableSetOf()
) : GroupsDelete {

    override fun groups(vararg groups: Group) = groups(groups.asSequence())

    override fun groups(groups: Collection<Group>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<Group>): GroupsDelete = apply {
        groupIds.addAll(groups.map { it.id ?: INVALID_ID })
    }

    override fun commit(): GroupsDelete.Result {
        if (groupIds.isEmpty() || !permissions.canInsertUpdateDelete()) {
            return GroupsDeleteFailed
        }

        val results = mutableMapOf<Long, Boolean>()
        for (groupId in groupIds) {
            results[groupId] = if (groupId == INVALID_ID) {
                false
            } else {
                contentResolver.applyBatch(GroupOperation.delete(groupId)) != null
            }
        }
        return GroupsDeleteResult(results)
    }

    override fun commitInOneTransaction(): Boolean = groupIds.isNotEmpty()
            && permissions.canInsertUpdateDelete()
            && contentResolver.applyBatch(GroupOperation.delete(groupIds)) != null

    private companion object {
        // A failed entry in the results so that Result.isSuccessful returns false.
        const val INVALID_ID = -1L
    }
}

private class GroupsDeleteResult(private val groupIdsResultMap: Map<Long, Boolean>) :
    GroupsDelete.Result {

    override val isSuccessful: Boolean by lazy { groupIdsResultMap.all { it.value } }

    override fun isSuccessful(group: Group): Boolean = group.id != null
            && groupIdsResultMap.getOrElse(group.id) { false }
}

private object GroupsDeleteFailed : GroupsDelete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: Group): Boolean = false
}
