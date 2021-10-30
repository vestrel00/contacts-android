package contacts.core.util

import contacts.core.Contacts
import contacts.core.entities.BlankRawContact
import contacts.core.entities.RawContact

/**
 * Returns the equivalent [RawContact] with all of the latest data.
 *
 * This may return null if the [RawContact] no longer exists or if [BlankRawContact.id] is null.
 *
 * Supports profile and non-profile RawContacts with native/custom data.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be returned
 * if the permission is not granted.
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
): RawContact? =
    id?.let { rawContactId ->
        contacts.findFirstRawContactWithId(rawContactId, cancel)
    }