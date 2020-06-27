package com.vestrel00.contacts.permissions.profile

import android.app.Activity
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.permissions.requestReadPermission
import com.vestrel00.contacts.profile.Profile
import com.vestrel00.contacts.profile.ProfileQuery

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [ProfileQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [ProfileQuery] instance.
 */
suspend fun Profile.queryWithPermission(activity: Activity): ProfileQuery {
    val permissions = permissions(activity)
    if (!permissions.canQuery()) {
        requestReadPermission(activity)
    }

    return query(activity)
}