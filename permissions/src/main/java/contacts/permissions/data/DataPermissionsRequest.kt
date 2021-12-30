package contacts.permissions.data

import contacts.core.ContactsPermissions
import contacts.core.data.Data
import contacts.core.data.DataDelete
import contacts.core.data.DataQueryFactory
import contacts.core.data.DataUpdate
import contacts.permissions.requestReadPermission
import contacts.permissions.requestWritePermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [DataQueryFactory] instance.
 *
 * If permission is already granted, then immediately returns a new [DataQueryFactory] instance.
 */
suspend fun Data.queryWithPermission(): DataQueryFactory {
    if (!contactsApi.permissions.canQuery()) {
        contactsApi.applicationContext.requestReadPermission()
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
    if (!contactsApi.permissions.canUpdateDelete()) {
        contactsApi.applicationContext.requestWritePermission()
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
    if (!contactsApi.permissions.canUpdateDelete()) {
        contactsApi.applicationContext.requestWritePermission()
    }

    return delete()
}