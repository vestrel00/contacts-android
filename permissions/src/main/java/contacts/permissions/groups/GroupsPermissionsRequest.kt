package contacts.permissions.groups

import contacts.core.ContactsPermissions
import contacts.core.groups.*
import contacts.permissions.accounts.requestGetAccountsPermission
import contacts.permissions.requestReadPermission
import contacts.permissions.requestWritePermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [GroupsQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [GroupsQuery] instance.
 */
suspend fun Groups.queryWithPermission(): GroupsQuery {
    if (!contactsApi.permissions.canQuery()) {
        contactsApi.applicationContext.requestReadPermission()
    }

    return query()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet granted,
 * suspends the current coroutine, requests for the permissions, and then returns a new
 * [GroupsInsert] instance.
 *
 * If permissions are already granted, then immediately returns a new [GroupsInsert] instance.
 */
suspend fun Groups.insertWithPermission(): GroupsInsert {
    if (!contactsApi.permissions.canInsert()) {
        contactsApi.applicationContext.requestWritePermission()
        contactsApi.applicationContext.requestGetAccountsPermission()
    }

    return insert()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet  granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [GroupsUpdate] instance.
 *
 * If permissions are already granted, then immediately returns a new [GroupsUpdate] instance.
 */
suspend fun Groups.updateWithPermission(): GroupsUpdate {
    if (!contactsApi.permissions.canUpdateDelete()) {
        contactsApi.applicationContext.requestWritePermission()
    }

    return update()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet  granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [GroupsDelete] instance.
 *
 * If permissions are already granted, then immediately returns a new [GroupsDelete] instance.
 */
suspend fun Groups.deleteWithPermission(): GroupsDelete? {
    if (!contactsApi.permissions.canUpdateDelete()) {
        contactsApi.applicationContext.requestWritePermission()
    }

    return delete()
}