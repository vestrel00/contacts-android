package com.vestrel00.contacts.groups

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.operation.GroupOperation

interface GroupsDelete {

    fun groups(vararg groups: Group): GroupsDelete

    fun groups(groups: Collection<Group>): GroupsDelete

    fun groups(groups: Sequence<Group>): GroupsDelete

    fun commit(): Result

    interface Result {

        val isSuccessful: Boolean

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
        // Do not add system groups to the delete queue!
        groupIds.addAll(groups.filter { !it.readOnly }.map { it.id })
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

    override fun isSuccessful(group: Group): Boolean =
        groupIdsResultMap.getOrElse(group.id) { false }
}

private object GroupsDeleteFailed : GroupsDelete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: Group): Boolean = false
}
