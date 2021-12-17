package contacts.core.util

import contacts.core.Contacts
import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.RawContact

/**
 * Returns the [RawContact] with the [ExistingDataEntity.rawContactId].
 *
 * This may return null if the [RawContact] no longer exists or if permissions are not granted.
 *
 * Supports profile/non-profile RawContacts with native/custom data.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun ExistingDataEntity.rawContact(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): RawContact? = contacts.findRawContactWithId(rawContactId, cancel)