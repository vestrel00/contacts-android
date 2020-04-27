package com.vestrel00.contacts.groups

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.operation.GroupOperation

/**
 * Deletes one or more groups from the groups table.
 *
 * FIXME? Expose this to consumers? For more details, see the DEV_NOTES "Groups; Deletion" section.
 * Marked as internal until it can be exposed to consumers.
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
 * In Kotlin and Java,
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
     * System groups, which have [Group.readOnly] set to true, cannot be deleted and are ignored
     * here.
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
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

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

    override fun groups(vararg groups: Group): GroupsDelete = groups(groups.asSequence())

    override fun groups(groups: Collection<Group>): GroupsDelete = groups(groups.asSequence())

    override fun groups(groups: Sequence<Group>): GroupsDelete = apply {
        // Do not add readOnly groups to the delete queue!
        groupIds.addAll(groups.filter { !it.readOnly }.map { it.id }.filterNotNull())
    }

    override fun commit(): GroupsDelete.Result {
        if (groupIds.isEmpty() || !permissions.canInsertUpdateDelete()) {
            return GroupsDeleteFailed
        }

        val results = mutableMapOf<Long, Boolean>()
        for (groupId in groupIds) {
            results[groupId] = deleteGroupWithId(groupId)
        }
        return GroupsDeleteResult(results)
    }

    private fun deleteGroupWithId(groupId: Long): Boolean {
        val operation = GroupOperation().delete(groupId)

        /*
         * Atomically delete the group row.
         *
         * Perform this single operation in a batch to be consistent with the other CRUD functions.
         */
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
        } catch (exception: Exception) {
            return false
        }

        return true
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
