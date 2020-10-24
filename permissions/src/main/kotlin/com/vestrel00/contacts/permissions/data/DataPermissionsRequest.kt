package com.vestrel00.contacts.permissions.data

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.data.Data
import com.vestrel00.contacts.data.DataDelete
import com.vestrel00.contacts.data.DataQuery
import com.vestrel00.contacts.data.DataUpdate
import com.vestrel00.contacts.permissions.requestReadPermission
import com.vestrel00.contacts.permissions.requestWritePermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [DataQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [DataQuery] instance.
 */
suspend fun Data.queryWithPermission(context: Context): DataQuery {
    val permissions = permissions(context)
    if (!permissions.canQuery()) {
        requestReadPermission(context)
    }

    return query(context)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet  granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [DataUpdate] instance.
 *
 * If permission is already granted, then immediately returns a new [DataUpdate] instance.
 */
suspend fun Data.updateWithPermission(context: Context): DataUpdate {
    val permissions = permissions(context)
    if (!permissions.canUpdateDelete()) {
        requestWritePermission(context)
    }

    return update(context)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [DataDelete] instance.
 *
 * If permissions are already granted, then immediately returns a new [DataDelete] instance.
 */
suspend fun Data.deleteWithPermission(context: Context): DataDelete {
    val permissions = permissions(context)
    if (!permissions.canUpdateDelete()) {
        requestWritePermission(context)
    }

    return delete(context)
}