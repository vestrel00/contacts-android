package contacts.core.entities.operation

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import contacts.core.Fields
import contacts.core.GroupMembershipField
import contacts.core.Include
import contacts.core.accounts.accountForRawContactWithId
import contacts.core.entities.*
import contacts.core.entities.mapper.groupMembershipMapper
import contacts.core.groups.Groups
import contacts.core.util.query

internal class GroupMembershipOperation(
    isProfile: Boolean,
    includeFields: Set<GroupMembershipField>,
    private val groups: Groups
) : AbstractDataOperation<GroupMembershipField, GroupMembershipEntity>(isProfile, includeFields) {

    override val mimeType = MimeType.GroupMembership

    override fun setValuesFromData(
        data: GroupMembershipEntity,
        setValue: (field: GroupMembershipField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.GroupMembership.GroupId, data.groupId)
    }

    /**
     * Inserts all of the [groupMemberships] that belong to the given [account].
     */
    fun insertForNewRawContact(
        groupMemberships: Collection<GroupMembershipEntity>,
        account: Account?
    ): List<ContentProviderOperation> = mutableListOf<ContentProviderOperation>().apply {
        if (includeFields.isEmpty()) {
            // No-op when entity is blank or no fields are included.
            return@apply
        }

        // Map of Group.id -> Group
        val accountGroups: MutableMap<Long, Group> = groups.query().accounts(account).find()
            .associateBy { it.id }
            .toMutableMap()

        // Ensure no duplicate group memberships by only comparing the groupId and that they belong
        // to the same account.
        groupMemberships
            .asSequence()
            .distinctBy { it.groupId }
            .filter { accountGroups[it.groupId] != null }
            .forEach { insertForNewRawContact(it)?.let(::add) }
    }

    /**
     * Provides the [ContentProviderOperation] for inserting or deleting the [groupMemberships] data
     * row(s) of the raw contact with the given [rawContactId]. A group membership cannot be updated
     * because it only contains an immutable reference to the group id.
     *
     * The given [groupMemberships] are compared to the memberships in the DB using
     * [GroupMembershipEntity.groupId] instead of the data row id. This allows consumers to pass
     * in new group membership entities without unnecessarily deleting rows in the DB with a
     * different row ID but the same group id.
     *
     * [GroupMembershipEntity]s that do not belong to the (nullable) account associated with the
     * [rawContactId] will be ignored. Also, memberships to default groups are never deleted.
     */
    fun updateInsertOrDelete(
        groupMemberships: Collection<GroupMembershipEntity>,
        rawContactId: Long,
        context: Context
    ): List<ContentProviderOperation> = mutableListOf<ContentProviderOperation>().apply {
        if (includeFields.isEmpty()) {
            // No-op when no fields are included.
            return@apply
        }

        val account: Account? = context.contentResolver.accountForRawContactWithId(rawContactId)

        // A map of Group.id -> Group
        val accountGroups: Map<Long, Group> =
            groups.query().accounts(account).find().associateBy { it.id }

        // A map of Group.id -> GroupMembership
        val groupMembershipsInDB: MutableMap<Long, GroupMembership> =
            context.contentResolver.getGroupMembershipsInDB(rawContactId)
                .asSequence()
                // There should not exist any memberships in the DB that does not belong to the same
                // account. Just in case though...
                .filter { membership ->
                    membership.groupId != null && accountGroups[membership.groupId] != null
                }
                .associateBy { membership ->
                    // There should be no null groupId at this point. This is just for casting the
                    // nullable groupId to a non-null long.
                    membership.groupId ?: Entity.INVALID_ID
                }
                .toMutableMap()

        // Ensure no duplicate group memberships by only comparing the groupId and that they belong
        // to the same account.
        groupMemberships
            .asSequence()
            .distinctBy { membership ->
                membership.groupId
            }
            .filter { membership ->
                membership.groupId != null && accountGroups[membership.groupId] != null
            }
            .forEach { membership ->
                // Remove this membership from the groupMembershipsInDB so that it will not be
                // deleted later down this function.
                if (groupMembershipsInDB.remove(membership.groupId) == null) {
                    // If the membership is not in the DB, insert it.
                    insertDataRowForRawContact(membership, rawContactId)?.let(::add)
                }
                // Else if the membership is in the DB, do nothing.
            }

        // Delete the remaining non-default groupMembershipsInDB.
        groupMembershipsInDB.values
            .asSequence()
            .filter { membership ->
                val groupId = membership.groupId
                // Do no delete memberships to the default group!
                groupId != null && !accountGroups.getValue(groupId).isDefaultGroup
            }
            .forEach { membership ->
                add(deleteDataRowWithId(membership.id))
            }
    }

    private fun ContentResolver.getGroupMembershipsInDB(rawContactId: Long):
            List<GroupMembership> =
        query(contentUri, INCLUDE, selectionWithMimeTypeForRawContact(rawContactId)) {
            mutableListOf<GroupMembership>().apply {
                val groupMembershipMapper = it.groupMembershipMapper()
                while (it.moveToNext()) {
                    add(groupMembershipMapper.value)
                }
            }
        } ?: emptyList()
}

private val INCLUDE = Include(Fields.DataId, Fields.GroupMembership.GroupId)