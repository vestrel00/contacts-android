package contacts.core.util

import contacts.core.Contacts
import contacts.core.GroupsFields
import contacts.core.`in`
import contacts.core.entities.Group
import contacts.core.entities.MutableGroup
import contacts.core.equalTo
import contacts.core.groups.GroupsInsert

/**
 * Returns the newly created [Group] or null if the insert operation failed.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be
 * returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun GroupsInsert.Result.group(
    contacts: Contacts, group: MutableGroup, cancel: () -> Boolean = { false }
): Group? = groupId(group)?.let { groupId ->
    contacts.groups().query()
        .where(GroupsFields.Id equalTo groupId)
        .find(cancel)
        .firstOrNull()
}

/**
 * Returns the newly created [Group]s (for those insert operations that succeeded).
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be
 * returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun GroupsInsert.Result.groups(contacts: Contacts, cancel: () -> Boolean = { false }): List<Group> =
    if (groupIds.isEmpty()) {
        emptyList()
    } else {
        contacts.groups().query()
            .where(GroupsFields.Id `in` groupIds)
            .find(cancel)
    }