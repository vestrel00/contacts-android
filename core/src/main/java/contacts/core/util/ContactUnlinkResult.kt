package contacts.core.util

import contacts.core.Contacts
import contacts.core.`in`
import contacts.core.aggregationexceptions.ContactUnlink
import contacts.core.entities.Contact

// Note that there is no need to handle isProfile here as ContactLinks operations do not support it.

/**
 * Returns all of the [Contact]s that are associated with each of the unlinked RawContacts.
 *
 * Returns an empty list if the link operation failed or permissions are not granted.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required.
 *
 * ## Cancellation
 *
 * To cancel this operation at any time, the [cancel] function should return true.
 *
 * This is useful when running this function in a background thread or coroutine.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun ContactUnlink.Result.contacts(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): List<Contact> = if (rawContactIds.isEmpty()) {
    emptyList()
} else {
    contacts.query().where { RawContact.Id `in` rawContactIds }.find(cancel)
}

