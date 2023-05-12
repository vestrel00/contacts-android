package contacts.permissions.aggregationexceptions

import contacts.core.ContactsPermissions
import contacts.core.aggregationexceptions.AggregationExceptions
import contacts.core.aggregationexceptions.ContactLink
import contacts.core.aggregationexceptions.ContactUnlink
import contacts.permissions.requestWritePermission

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet  granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [ContactLink] instance.
 *
 * If permission is already granted, then immediately returns a new [ContactLink] instance.
 */
suspend fun AggregationExceptions.linkWithPermission(): ContactLink {
    if (!contactsApi.permissions.canUpdateDelete()) {
        requestWritePermission()
    }

    return link()
}

/**
 * If [ContactsPermissions.WRITE_PERMISSION] is not yet  granted, suspends the current coroutine,
 * requests for the permissions, and then returns a new [ContactUnlink] instance.
 *
 * If permission is already granted, then immediately returns a new [ContactUnlink] instance.
 */
suspend fun AggregationExceptions.unlinkWithPermission(): ContactUnlink {
    if (!contactsApi.permissions.canUpdateDelete()) {
        requestWritePermission()
    }

    return unlink()
}