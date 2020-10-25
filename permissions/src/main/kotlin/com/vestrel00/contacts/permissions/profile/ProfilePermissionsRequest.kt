package com.vestrel00.contacts.permissions.profile

import android.content.Context
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
suspend fun Profile.queryWithPermission(context: Context): ProfileQuery {
    val permissions = permissions(context)
    if (!permissions.canQuery()) {
        requestReadPermission(context)
    }

    return query(context)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet
 * granted, suspends the current coroutine, requests for the permission, and then returns a new
 * [ProfileInsert] instance.
 *
 * If permission is already granted, then immediately returns a new [ProfileInsert] instance.
 */
suspend fun Profile.insertWithPermission(context: Context): ProfileInsert {
    val permissions = permissions(context)
    if (!permissions.canInsert()) {
        requestWritePermission(context)
        requestGetAccountsPermission(context)
    }

    return insert(context)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [ProfileUpdate] instance.
 *
 * If permission is already granted, then immediately returns a new [ProfileUpdate] instance.
 */
suspend fun Profile.updateWithPermission(context: Context): ProfileUpdate {
    val permissions = permissions(context)
    if (!permissions.canUpdateDelete()) {
        requestWritePermission(context)
    }

    return update(context)
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [ProfileDelete] instance.
 *
 * If permission is already granted, then immediately returns a new [ProfileDelete] instance.
 */
suspend fun Profile.deleteWithPermission(context: Context): ProfileDelete {
    val permissions = permissions(context)
    if (!permissions.canUpdateDelete()) {
        requestWritePermission(context)
    }

    return delete(context)
}