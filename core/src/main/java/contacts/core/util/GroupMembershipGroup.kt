package contacts.core.util

import contacts.core.Contacts
import contacts.core.GroupsFields
import contacts.core.`in`
import contacts.core.entities.Group
import contacts.core.entities.GroupMembership
import contacts.core.equalTo

/**
 * Returns the [Group] referenced by this membership.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be returned
 * if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun GroupMembership.group(contacts: Contacts, cancel: () -> Boolean = { false }): Group? =
    groupId?.let {
        contacts.groups().query().where(GroupsFields.Id equalTo it).find(cancel).first()
    }

/**
 * Returns the groups referenced by these memberships.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be returned
 * if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Collection<GroupMembership>.groups(
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