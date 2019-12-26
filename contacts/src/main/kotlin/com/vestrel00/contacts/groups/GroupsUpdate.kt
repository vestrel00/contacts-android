package com.vestrel00.contacts.groups

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.entities.MutableGroup
import com.vestrel00.contacts.entities.operation.GroupOperation

interface GroupsUpdate {

    fun groups(vararg groups: MutableGroup?): GroupsUpdate

    fun groups(groups: Collection<MutableGroup?>): GroupsUpdate

    fun groups(groups: Sequence<MutableGroup?>): GroupsUpdate

    fun commit(): Result

    interface Result {

        val isSuccessful: Boolean

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
    private val groups: MutableSet<MutableGroup> = mutableSetOf()
) : GroupsUpdate {

    override fun groups(vararg groups: MutableGroup?): GroupsUpdate =
        groups(groups.asSequence())

    override fun groups(groups: Collection<MutableGroup?>): GroupsUpdate =
        groups(groups.asSequence())

    override fun groups(groups: Sequence<MutableGroup?>): GroupsUpdate = apply {
        val existingGroups = groups
            .filter { it != null && it.hasValidId() && !it.readOnly }
            .map { it!! }

        this.groups.addAll(existingGroups)
    }

    override fun commit(): GroupsUpdate.Result {
        if (groups.isEmpty() || !permissions.canInsertUpdateDelete()) {
            return GroupsUpdateFailed
        }

        val results = mutableMapOf<Long, Boolean>()
        for (group in groups) {
            results[group.id] = updateGroup(group)
        }
        return GroupsUpdateResult(results)
    }

    private fun updateGroup(group: MutableGroup): Boolean {
        val operation = GroupOperation().update(group)

        /*
         * Atomically update the group row.
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

private class GroupsUpdateResult(private val groupIdsResultMap: Map<Long, Boolean>) :
    GroupsUpdate.Result {

    override val isSuccessful: Boolean by lazy { groupIdsResultMap.all { it.value } }

    override fun isSuccessful(group: MutableGroup): Boolean =
        groupIdsResultMap.getOrElse(group.id) { false }
}

private object GroupsUpdateFailed : GroupsUpdate.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: MutableGroup): Boolean = false
}
