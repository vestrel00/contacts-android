package contacts.core.groups

import android.content.ContentResolver
import contacts.core.Contacts
import contacts.core.ContactsPermissions
import contacts.core.entities.GroupEntity
import contacts.core.entities.operation.GroupsOperation
import contacts.core.util.applyBatch
import contacts.core.util.unsafeLazy

/**
 * Deletes one or more groups from the groups table.
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
 *
 * ## Notes
 *
 * Prior to Android 8.0 (Oreo, API 26), group deletion is unpredictable. Groups that are marked for
 * deletion remain in the DB and is still shown in the native Contacts app. Sometimes they do get
 * deleted at some point but the trigger for the actual deletion eludes me.
 *
 * The native Contacts app (prior to API 26) does NOT support group deletion perhaps because groups
 * syncing isn't implemented or at least not to the same extent as contacts syncing. Therefore, this
 * library will also not support group deletion for API versions lower than 26.
 *
 * DO NOT USE THIS ON API VERSION BELOW 26! Or use at your own peril =)
 */
interface GroupsDelete {

    /**
     * Adds the given [groups] to the delete queue, which will be deleted on [commit].
     *
     * Read-only groups will be ignored and result in a failed operation.
     */
    fun groups(vararg groups: GroupEntity): GroupsDelete

    /**
     * See [GroupsDelete.groups].
     */
    fun groups(groups: Collection<GroupEntity>): GroupsDelete

    /**
     * See [GroupsDelete.groups].
     */
    fun groups(groups: Sequence<GroupEntity>): GroupsDelete

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
        fun isSuccessful(group: GroupEntity): Boolean
    }
}

@Suppress("FunctionName")
internal fun GroupsDelete(contacts: Contacts): GroupsDelete = GroupsDeleteImpl(
    contacts.applicationContext.contentResolver,
    contacts.permissions
)

private class GroupsDeleteImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val groupIds: MutableSet<Long> = mutableSetOf()
) : GroupsDelete {

    override fun toString(): String =
        """
            GroupsDelete {
                groupIds: $groupIds
            }
        """.trimIndent()

    override fun groups(vararg groups: GroupEntity) = groups(groups.asSequence())

    override fun groups(groups: Collection<GroupEntity>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<GroupEntity>): GroupsDelete = apply {
        groupIds.addAll(groups.map {
            if (it.readOnly) { // do not attempt to delete read-only groups
                INVALID_ID
            } else {
                it.id ?: INVALID_ID
            }
        })
    }

    override fun commit(): GroupsDelete.Result {
        if (groupIds.isEmpty() || !permissions.canUpdateDelete()) {
            return GroupsDeleteFailed()
        }

        val results = mutableMapOf<Long, Boolean>()
        for (groupId in groupIds) {
            results[groupId] = if (groupId == INVALID_ID) {
                false
            } else {
                contentResolver.applyBatch(GroupsOperation().delete(groupId)) != null
            }
        }
        return GroupsDeleteResult(results)
    }

    override fun commitInOneTransaction(): Boolean = permissions.canUpdateDelete()
            && groupIds.isNotEmpty()
            && !groupIds.contains(INVALID_ID)
            && contentResolver.applyBatch(GroupsOperation().delete(groupIds)) != null

    private companion object {
        // A failed entry in the results so that Result.isSuccessful returns false.
        const val INVALID_ID = -1L
    }
}

private class GroupsDeleteResult(private val groupIdsResultMap: Map<Long, Boolean>) :
    GroupsDelete.Result {

    override val isSuccessful: Boolean by unsafeLazy { groupIdsResultMap.all { it.value } }

    override fun isSuccessful(group: GroupEntity): Boolean {
        val groupId = group.id ?: return false
        return groupIdsResultMap.getOrElse(groupId) { false }
    }
}

private class GroupsDeleteFailed : GroupsDelete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: GroupEntity): Boolean = false
}
