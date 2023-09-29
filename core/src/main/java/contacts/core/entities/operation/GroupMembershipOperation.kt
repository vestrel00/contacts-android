package contacts.core.entities.operation

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentResolver
import contacts.core.Contacts
import contacts.core.Fields
import contacts.core.GroupMembershipField
import contacts.core.Include
import contacts.core.RawContactsFields
import contacts.core.contentResolver
import contacts.core.entities.Entity
import contacts.core.entities.Group
import contacts.core.entities.GroupMembership
import contacts.core.entities.GroupMembershipEntity
import contacts.core.entities.MimeType
import contacts.core.entities.cursor.account
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.mapper.groupMembershipMapper
import contacts.core.equalTo
import contacts.core.util.contacts
import contacts.core.util.isProfileId
import contacts.core.util.nullIfNotInSystem
import contacts.core.util.query
import contacts.core.util.rawContactsUri

internal class GroupMembershipOperation(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<GroupMembershipField>?
) : AbstractDataOperation<GroupMembershipField, GroupMembershipEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

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
        groupsMap: Map<Long, Group>?, // Map of Group.id -> Group
        rawContactIdOpIndex: Int
    ): List<ContentProviderOperation> = buildList {
        if (groupMemberships.isEmpty() || (includeFields != null && includeFields.isEmpty())) {
            // No-op when entity is blank or no fields are included.
            return@buildList
        }

        // Ensure no duplicate group memberships by only comparing the groupId and that they belong
        // to the same account.
        groupMemberships
            .asSequence()
            .distinctBy { it.groupId }
            .filter {
                // Do not filter if groups map is not provided. If it is provided, then filter out
                // memberships that are not in it.
                groupsMap == null || groupsMap[it.groupId] != null
            }
            .forEach { insertForNewRawContact(it, rawContactIdOpIndex)?.let(::add) }
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
        contactsApi: Contacts,
        cancel: () -> Boolean,
    ): List<ContentProviderOperation> = buildList {
        if (includeFields != null && includeFields.isEmpty()) {
            // No-op when no fields are included.
            return@buildList
        }

        val account: Account? = contactsApi.accountForRawContactWithId(rawContactId)

        // A map of Group.id -> Group
        val accountGroups: Map<Long, Group> =
            contactsApi.groups().query().accounts(account).find(cancel).associateBy { it.id }

        // A map of Group.id -> GroupMembership
        val groupMembershipsInDB: MutableMap<Long, GroupMembership> =
            contactsApi.contentResolver.getGroupMembershipsInDB(rawContactId)
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

    private fun ContentResolver.getGroupMembershipsInDB(rawContactId: Long): List<GroupMembership> =
        query(contentUri, INCLUDE, selectionWithMimeTypeForRawContact(rawContactId)) {
            buildList {
                val groupMembershipMapper = it.groupMembershipMapper()
                while (it.moveToNext()) {
                    add(groupMembershipMapper.value)
                }
            }
        } ?: emptyList()
}

/**
 * Returns the Account, based on the values in the RawContacts table, for the RawContact with the
 * given [rawContactId].
 *
 * This will return a null Account if the non-null account name and type stored in the RawContacts
 * table is not in the system. In other words, this will return null for invalid Accounts.
 *
 * This only requires [contacts.core.ContactsPermissions.READ_PERMISSION].
 */
private fun Contacts.accountForRawContactWithId(rawContactId: Long): Account? =
    contentResolver.query(
        rawContactsUri(rawContactId.isProfileId),
        Include(RawContactsFields.AccountName, RawContactsFields.AccountType),
        RawContactsFields.Id equalTo rawContactId
    ) {
        val account = it.getNextOrNull { it.rawContactsCursor().account() }
        account.nullIfNotInSystem(this)
    }

private val INCLUDE = Include(Fields.DataId, Fields.GroupMembership.GroupId)