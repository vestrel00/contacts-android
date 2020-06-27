package com.vestrel00.contacts.util

import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.GroupMembership

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
 * Returns the first default group found in [this] collection of groups.
 */
fun Collection<Group>.defaultGroup(): Group? = firstOrNull { it.isDefaultGroup }

/**
 * Returns the first favorites group found in [this] collection of groups.
 */
fun Collection<Group>.favoritesGroup(): Group? = firstOrNull { it.isFavoritesGroup }

/**
 * Returns [this] sequence of [Group]s as list of [GroupMembership] that may be used for Contacts
 * and RawContacts insert and update operations.
 */
fun Sequence<Group>.toGroupMemberships(): Sequence<GroupMembership> = map { it.toGroupMembership() }

/**
 * Returns the first default group found in [this] sequence of groups.
 */
fun Sequence<Group>.defaultGroup(): Group? = firstOrNull { it.isDefaultGroup }

/**
 * Returns the first favorites group found in [this] sequence of groups.
 */
fun Sequence<Group>.favoritesGroup(): Group? = firstOrNull { it.isFavoritesGroup }