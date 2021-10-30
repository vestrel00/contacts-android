package contacts.permissions.data

import contacts.core.ContactsPermissions
import contacts.core.data.Data
import contacts.core.data.DataDelete
import contacts.core.data.DataQuery
import contacts.core.data.DataUpdate
import contacts.permissions.requestReadPermission
import contacts.permissions.requestWritePermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [DataQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [DataQuery] instance.
 */
suspend fun Data.queryWithPermission(): DataQuery {
    if (!permissions.canQuery) {
        applicationContext.requestReadPermission()
    }

    return query()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet  granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [DataUpdate] instance.
 *
 * If permission is already granted, then immediately returns a new [DataUpdate] instance.
 */
suspend fun Data.updateWithPermission(): DataUpdate {
    if (!permissions.canUpdateDelete) {
        applicationContext.requestWritePermission()
    }

    return update()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [DataDelete] instance.
 *
 * If permissions are already granted, then immediately returns a new [DataDelete] instance.
 */
suspend fun Data.deleteWithPermission(): DataDelete {
    if (!permissions.canUpdateDelete) {
        applicationContext.requestWritePermission()
    }

    return delete()
}