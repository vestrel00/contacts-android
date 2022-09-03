package contacts.core.util

import contacts.core.entities.ExistingGroupEntity
import contacts.core.entities.NewGroupMembership

/**
 * Returns a new [NewGroupMembership] instance that may be used for Contacts and RawContacts insert
 * and update operations.
 */
fun ExistingGroupEntity.newMembership() = NewGroupMembership(groupId = id, isRedacted = isRedacted)

/**
 * Returns [this] collection of [ExistingGroupEntity]s as list of [NewGroupMembership] that may be
 * used for Contacts and RawContacts insert and update operations.
 */
fun Collection<ExistingGroupEntity>.newMemberships(): List<NewGroupMembership> =
    map { it.newMembership() }

/**
 * Returns [this] sequence of [ExistingGroupEntity]s as list of [NewGroupMembership] that may be
 * used for Contacts and RawContacts insert and update operations.
 */
fun Sequence<ExistingGroupEntity>.newMemberships(): Sequence<NewGroupMembership> =
    map { it.newMembership() }