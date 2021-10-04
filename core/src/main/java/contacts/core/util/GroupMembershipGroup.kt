package contacts.core.util

import android.content.Context
import contacts.core.GroupsFields
import contacts.core.`in`
import contacts.core.entities.Group
import contacts.core.entities.GroupMembership
import contacts.core.equalTo
import contacts.core.groups.GroupsQuery

/**
 * Returns the [Group] referenced by this membership.
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
fun GroupMembership.group(context: Context, cancel: () -> Boolean = { false }): Group? =
    groupId?.let {
        GroupsQuery(context).where(GroupsFields.Id equalTo it).find(cancel).first()
    }

/**
 * Returns the groups referenced by these memberships.
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
    context: Context, cancel: () -> Boolean = { false }
): List<Group> {
    val membershipIds = mapNotNull { it.groupId }

    return if (membershipIds.isEmpty()) {
        emptyList()
    } else {
        GroupsQuery(context)
            .where(GroupsFields.Id `in` membershipIds)
            .find(cancel)
    }
}