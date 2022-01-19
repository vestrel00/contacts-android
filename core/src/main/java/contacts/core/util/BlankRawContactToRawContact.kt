package contacts.core.util

import contacts.core.Contacts
import contacts.core.entities.BlankRawContact
import contacts.core.entities.RawContact

/**
 * Returns the equivalent [RawContact] with all of the latest data.
 *
 * This may return null if the [RawContact] no longer exists or if permissions are not ranted.
 *
 * Supports profile and non-profile RawContacts with native/custom data.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be
 * returned if the permission is not granted.
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
fun BlankRawContact.toRawContact(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): RawContact? = contacts.findRawContactWithId(id, cancel)