package contacts.core.util

import contacts.core.entities.Group
import contacts.core.entities.NewGroupMembership

// Dev note: Using concrete type as the function receiver instead of the generic type in order to
// prevent consumers from constructing immutable types using manually created types.

/**
 * Returns a new [NewGroupMembership] instance that may be used for Contacts and RawContacts insert
 * and update operations.
 */
fun Group.newMembership() = NewGroupMembership(groupId = id, isRedacted = isRedacted)

/**
 * Returns [this] collection of [Group]s as list of [NewGroupMembership] that may be used for
 * Contacts and RawContacts insert and update operations.
 */
fun Collection<Group>.newMemberships(): List<NewGroupMembership> = map { it.newMembership() }

/**
 * Returns [this] sequence of [Group]s as list of [NewGroupMembership] that may be used for Contacts
 * and RawContacts insert and update operations.
 */
fun Sequence<Group>.newMemberships(): Sequence<NewGroupMembership> = map { it.newMembership() }