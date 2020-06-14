package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.GroupsFields
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.GroupMembership
import com.vestrel00.contacts.equalTo
import com.vestrel00.contacts.groups.GroupsQuery

/**
 * Returns the [Group] referenced by this membership.
 *
 * ## Permissions
 *
 * The [com.vestrel00.contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null
 * will be returned if the permission is not granted.
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