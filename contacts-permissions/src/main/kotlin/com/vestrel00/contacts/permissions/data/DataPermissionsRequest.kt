package com.vestrel00.contacts.permissions.data

import android.app.Activity
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.data.Data
import com.vestrel00.contacts.data.DataQuery
import com.vestrel00.contacts.permissions.requestQueryPermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [DataQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [DataQuery] instance.
 */
suspend fun Data.queryWithPermission(activity: Activity): DataQuery {
    val permissions = permissions(activity)
    if (!permissions.canQuery()) {
        permissions.requestQueryPermission(activity)
    }

    return query(activity)
}