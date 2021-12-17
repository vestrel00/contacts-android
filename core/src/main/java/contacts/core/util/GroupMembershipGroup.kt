package contacts.core.util

import contacts.core.Contacts
import contacts.core.GroupsFields
import contacts.core.`in`
import contacts.core.entities.Group
import contacts.core.entities.GroupMembershipEntity
import contacts.core.equalTo

// Dev note: Using concrete type as the function receiver instead of the generic type in order to
// prevent consumers from constructing immutable types using manually created types.

/**
 * Returns the [Group] referenced by this membership.
 *
 * This may return null if the group no longer exists or permissions are not granted.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun GroupMembershipEntity.group(contacts: Contacts, cancel: () -> Boolean = { false }): Group? =
    groupId?.let {
        contacts.groups().query().where(GroupsFields.Id equalTo it).find(cancel).first()
    }

/**
 * Returns the groups referenced by these memberships.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Collection<GroupMembershipEntity>.groups(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): List<Group> {
    val membershipIds = mapNotNull { it.groupId }

    return if (membershipIds.isEmpty()) {
        emptyList()
    } else {
        contacts.groups().query()
            .where(GroupsFields.Id `in` membershipIds)
            .find(cancel)
    }
}