package contacts.core.util

import contacts.core.entities.Group
import contacts.core.entities.GroupMembership

/**
 * Returns a new [GroupMembership] instance that may be used for Contacts and RawContacts insert
 * and update operations.
 */
fun Group.toGroupMembership(): GroupMembership = GroupMembership(
    id = null,
    rawContactId = null,
    contactId = null,
    groupId = id,
    isPrimary = false,
    isSuperPrimary = false
)

/**
 * Returns [this] collection of [Group]s as list of [GroupMembership] that may be used for Contacts
 * and RawContacts insert and update operations.
 */
fun Collection<Group>.toGroupMemberships(): List<GroupMembership> = map { it.toGroupMembership() }

/**
 * Returns [this] sequence of [Group]s as list of [GroupMembership] that may be used for Contacts
 * and RawContacts insert and update operations.
 */
fun Sequence<Group>.toGroupMemberships(): Sequence<GroupMembership> = map { it.toGroupMembership() }