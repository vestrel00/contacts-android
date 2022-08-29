package contacts.permissions.sim

import contacts.core.ContactsPermissions
import contacts.core.sim.*
import contacts.permissions.requestReadPermission
import contacts.permissions.requestWritePermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [SimContactsQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [SimContactsQuery] instance.
 */
suspend fun SimContacts.queryWithPermission(): SimContactsQuery {
    if (!contactsApi.permissions.canQuery()) {
        requestReadPermission()
    }

    return query()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] are not yet granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [SimContactsInsert] instance.
 *
 * If permissions are already granted, then immediately returns a new [SimContactsInsert] instance.
 */
suspend fun SimContacts.insertWithPermission(): SimContactsInsert {
    if (!contactsApi.permissions.canInsertToSim()) {
        requestWritePermission()
    }

    return insert()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] are not yet granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [SimContactsUpdate] instance.
 *
 * If permissions are already granted, then immediately returns a new [SimContactsUpdate] instance.
 */
suspend fun SimContacts.updateWithPermission(): SimContactsUpdate {
    if (!contactsApi.permissions.canUpdateDelete()) {
        requestWritePermission()
    }

    return update()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet  granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [SimContactsDelete] instance.
 *
 * If permissions are already granted, then immediately returns a new [SimContactsDelete] instance.
 */
suspend fun SimContacts.deleteWithPermission(): SimContactsDelete {
    if (!contactsApi.permissions.canUpdateDelete()) {
        requestWritePermission()
    }

    return delete()
}