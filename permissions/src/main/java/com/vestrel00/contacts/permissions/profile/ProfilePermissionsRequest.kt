package com.vestrel00.contacts.permissions.profile

import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.permissions.accounts.requestGetAccountsPermission
import com.vestrel00.contacts.permissions.requestReadPermission
import com.vestrel00.contacts.permissions.requestWritePermission
import com.vestrel00.contacts.profile.*

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [ProfileQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [ProfileQuery] instance.
 */
suspend fun Profile.queryWithPermission(): ProfileQuery {
    val permissions = permissions()
    if (!permissions.canQuery()) {
        applicationContext.requestReadPermission()
    }

    return query()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permission, and then returns a new
 * [ProfileInsert] instance.
 *
 * If permission is already granted, then immediately returns a new [ProfileInsert] instance.
 */
suspend fun Profile.insertWithPermission(): ProfileInsert {
    val permissions = permissions()
    if (!permissions.canInsert()) {
        applicationContext.requestWritePermission()
        applicationContext.requestGetAccountsPermission()
    }

    return insert()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [ProfileUpdate] instance.
 *
 * If permission is already granted, then immediately returns a new [ProfileUpdate] instance.
 */
suspend fun Profile.updateWithPermission(): ProfileUpdate {
    val permissions = permissions()
    if (!permissions.canUpdateDelete()) {
        applicationContext.requestWritePermission()
    }

    return update()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [ProfileDelete] instance.
 *
 * If permission is already granted, then immediately returns a new [ProfileDelete] instance.
 */
suspend fun Profile.deleteWithPermission(): ProfileDelete {
    val permissions = permissions()
    if (!permissions.canUpdateDelete()) {
        applicationContext.requestWritePermission()
    }

    return delete()
}