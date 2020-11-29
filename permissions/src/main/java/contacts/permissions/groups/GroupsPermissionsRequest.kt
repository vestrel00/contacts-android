package contacts.permissions.groups

import contacts.ContactsPermissions
import contacts.groups.Groups
import contacts.groups.GroupsInsert
import contacts.groups.GroupsQuery
import contacts.groups.GroupsUpdate
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
    if (!permissions.canQuery()) {
        applicationContext.requestReadPermission()
    }

    return query()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permissions, and then returns a new
 * [GroupsInsert] instance.
 *
 * If permissions are already granted, then immediately returns a new [GroupsInsert] instance.
 */
suspend fun Groups.insertWithPermission(): GroupsInsert {
    if (!permissions.canInsert()) {
        applicationContext.requestWritePermission()
        applicationContext.requestGetAccountsPermission()
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
    if (!permissions.canUpdateDelete()) {
        applicationContext.requestWritePermission()
    }

    return update()
}