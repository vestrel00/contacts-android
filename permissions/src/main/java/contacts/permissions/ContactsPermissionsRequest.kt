package contacts.permissions

import contacts.core.*
import contacts.permissions.accounts.requestGetAccountsPermission

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [Query] instance.
 *
 * If permission is already granted, then immediately returns a new [Query] instance.
 */
suspend fun Contacts.queryWithPermission(): Query {
    if (!permissions.canQuery()) {
        requestReadPermission()
    }

    return query()
}

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [RawContactsQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [RawContactsQuery] instance.
 */
suspend fun Contacts.rawContactsQueryWithPermission(): RawContactsQuery {
    if (!permissions.canQuery()) {
        requestReadPermission()
    }

    return rawContactsQuery()
}

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [BroadQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [BroadQuery] instance.
 */
suspend fun Contacts.broadQueryWithPermission(): BroadQuery {
    if (!permissions.canQuery()) {
        requestReadPermission()
    }

    return broadQuery()
}

/**
 * If [ContactsPermissions.READ_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [PhoneLookupQuery] instance.
 *
 * If permission is already granted, then immediately returns a new [PhoneLookupQuery] instance.
 */
suspend fun Contacts.phoneLookupQueryWithPermission(): PhoneLookupQuery {
    if (!permissions.canQuery()) {
        requestReadPermission()
    }

    return phoneLookupQuery()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] and
 * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are not yet granted,
 * suspends the current coroutine, requests for the permissions, and then returns a new [Insert]
 * instance.
 *
 * If permissions are already granted, then immediately returns a new [Insert] instance.
 */
suspend fun Contacts.insertWithPermission(): Insert {
    if (!permissions.canInsert()) {
        requestWritePermission()
        requestGetAccountsPermission()
    }

    return insert()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [Update] instance.
 *
 * If permissions are already granted, then immediately returns a new [Update] instance.
 */
suspend fun Contacts.updateWithPermission(): Update {
    if (!permissions.canUpdateDelete()) {
        requestWritePermission()
    }

    return update()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet granted, suspends the current coroutine,
 * requests for the permission, and then returns a new [Delete] instance.
 *
 * If permission is already granted, then immediately returns a new [Delete] instance.
 */
suspend fun Contacts.deleteWithPermission(): Delete {
    if (!permissions.canUpdateDelete()) {
        requestWritePermission()
    }

    return delete()
}

/**
 * Requests the [ContactsPermissions.READ_PERMISSION]. The current coroutine is suspended until the
 * user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestReadPermission(): Boolean =
    requestContactsPermission(ContactsPermissions.READ_PERMISSION)

/**
 * Requests the [ContactsPermissions.WRITE_PERMISSION]. The current coroutine is suspended until
 * the user either grants or denies the permissions request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestWritePermission(): Boolean =
    requestContactsPermission(ContactsPermissions.WRITE_PERMISSION)

private suspend fun requestContactsPermission(permission: String): Boolean =
    requestPermission(
        permission,
        R.string.contacts_request_permission_title,
        R.string.contacts_request_permission_description
    )