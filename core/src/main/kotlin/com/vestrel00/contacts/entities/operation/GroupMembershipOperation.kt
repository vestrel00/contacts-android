package com.vestrel00.contacts.entities.operation

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.Field
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.accounts.accountForRawContactWithId
import com.vestrel00.contacts.entities.GroupMembership
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.mapper.groupMembershipMapper
import com.vestrel00.contacts.groups.GroupsQuery
import com.vestrel00.contacts.util.query

internal class GroupMembershipOperation(private val isProfile: Boolean) :
    AbstractCommonDataOperation<GroupMembership>(isProfile) {

    override val mimeType = MimeType.GROUP_MEMBERSHIP

    override fun setData(
        data: GroupMembership, setValue: (field: Field, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.GroupMembership.GroupId, data.groupId)
    }

    /**
     * Inserts all of the [groupMemberships] that belong to the given [account].
     */
    fun insert(
        groupMemberships: Collection<GroupMembership>,
        account: Account,
        context: Context
    ): List<ContentProviderOperation> = mutableListOf<ContentProviderOperation>().apply {

        val accountGroups = GroupsQuery(context).accounts(account).find()
            .asSequence()
            .associateBy { it.id }
            .toMutableMap()

        // Ensure no duplicate group memberships by only comparing the groupId and that they belong
        // to the same account.
        groupMemberships
            .asSequence()
            .distinctBy { it.groupId }
            .filter { accountGroups[it.groupId] != null }
            .forEach { insert(it)?.let(::add) }
    }

    /**
     * Provides the [ContentProviderOperation] for inserting or deleting the [groupMemberships] data
     * row(s) of the raw contact with the given [rawContactId]. A group membership cannot be updated
     * because it only contains an immutable reference to the group id.
     *
     * [GroupMembership]s that do not belong to the Account associated with the [rawContactId] will
     * be ignored. Also, memberships to default groups are never deleted.
     */
    fun updateInsertOrDelete(
        groupMemberships: Collection<GroupMembership>,
        rawContactId: Long,
        context: Context
    ): List<ContentProviderOperation> = mutableListOf<ContentProviderOperation>().apply {

        // Groups must always be associated with an account. No account, no group operation.
        val account = context.contentResolver
            .accountForRawContactWithId(rawContactId, isProfile) ?: return emptyList()
        val accountGroups = GroupsQuery(context).accounts(account).find()
            // This is the same as GroupMembership.groupId.
            .associateBy { it.id }

        val groupMembershipsInDB = context.contentResolver.getGroupMembershipsInDB(rawContactId)
            .asSequence()
            // There should not exist any memberships in the DB that does not belong to the same
            // account. Just in case though...
            .filter { accountGroups[it.groupId] != null }
            .associateBy { it.groupId }
            .toMutableMap()

        // Ensure no duplicate group memberships by only comparing the groupId and that they belong
        // to the same account.
        groupMemberships
            .asSequence()
            .distinctBy { it.groupId }
            .filter { accountGroups[it.groupId] != null }
            .forEach { groupMembership ->
                // Remove this groupMembership from the groupMembershipsInDB.
                if (groupMembershipsInDB.remove(groupMembership.groupId) == null) {
                    // If the groupMembership is not in the DB, insert it.
                    add(insertDataRow(groupMembership, rawContactId))
                }
                // Else if the groupMembership is in the DB, do nothing.
            }

        // Delete the remaining non-default groupMembershipsInDB.
        groupMembershipsInDB.values
            .asSequence()
            .filter {
                val group = accountGroups.getValue(it.groupId)
                !group.isDefaultGroup
            }
            .map { it.id }
            .filterNotNull()
            .forEach { groupMembershipId ->
                add(deleteDataRowWithId(groupMembershipId))
            }
    }

    private fun ContentResolver.getGroupMembershipsInDB(rawContactId: Long):
            List<GroupMembership> = query(contentUri, INCLUDE, selection(rawContactId)) {

        mutableListOf<GroupMembership>().apply {
            val groupMembershipMapper = it.groupMembershipMapper()
            while (it.moveToNext()) {
                add(groupMembershipMapper.value)
            }
        }
    } ?: emptyList()
}

private val INCLUDE = Include(Fields.DataId, Fields.GroupMembership.GroupId)