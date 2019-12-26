package com.vestrel00.contacts.entities.operation

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.AbstractField
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.entities.GroupMembership
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.cursor.GroupMembershipCursor
import com.vestrel00.contacts.entities.mapper.GroupMembershipMapper
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.groups.Groups
import com.vestrel00.contacts.util.account
import com.vestrel00.contacts.util.accountForRawContactWithId

internal class GroupMembershipOperation : AbstractDataOperation<GroupMembership>() {

    override val mimeType = MimeType.GROUP_MEMBERSHIP

    override fun setData(
        data: GroupMembership, setValue: (field: AbstractField, dataValue: Any?) -> Unit
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

        val accountGroups = Groups().query(context).account(account).find()
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
     * Provides the [ContentProviderOperation] for updating, inserting, or deleting the
     * [groupMemberships] data row(s) of the raw contact with the given [rawContactId].
     *
     * [GroupMembership]s that do not belong to the given [account] will be ignored. Also,
     * memberships to default groups are never deleted.
     */
    fun updateInsertOrDelete(
        groupMemberships: Collection<GroupMembership>,
        rawContactId: Long,
        context: Context
    ): List<ContentProviderOperation> = mutableListOf<ContentProviderOperation>().apply {

        // Groups must always be associated with an account. No account, no group operation.
        val account = accountForRawContactWithId(rawContactId, context) ?: return emptyList()
        val accountGroups = Groups().query(context).account(account).find()
            .asSequence()
            .associateBy { it.id } // This is the same as GroupMembership.groupId.
            .toMutableMap()

        val groupMembershipsInDB = getGroupMembershipsInDB(rawContactId, context.contentResolver)
            .asSequence()
            // There should'nt exist any memberships in the DB that does not belong to the same
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
                val group = accountGroups[it.groupId]!! // This unwrap is safe here.
                !group.isDefaultGroup
            }
            .forEach { groupMembership ->
                add(deleteDataRowWithId(groupMembership.id))
            }
    }

    private fun getGroupMembershipsInDB(
        rawContactId: Long,
        contentResolver: ContentResolver
    ): MutableList<GroupMembership> = mutableListOf<GroupMembership>().apply {

        val cursor = contentResolver.query(
            Table.DATA.uri,
            INCLUDE,
            "${selection(rawContactId)}",
            null,
            null
        )
        if (cursor != null) {
            val groupMembershipMapper = GroupMembershipMapper(GroupMembershipCursor(cursor))
            while (cursor.moveToNext()) {
                add(groupMembershipMapper.groupMembership)
            }
            cursor.close()
        }
    }

    private companion object {
        private val INCLUDE = Include(Fields.Id, Fields.GroupMembership.GroupId).columnNames
    }
}