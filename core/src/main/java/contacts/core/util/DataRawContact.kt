package contacts.core.util

import contacts.core.Contacts
import contacts.core.entities.DataEntity
import contacts.core.entities.RawContact

/**
 * Returns the [RawContact] with the [DataEntity.rawContactId].
 *
 * This may return null if the [RawContact] no longer exists or if [DataEntity.rawContactId] is null
 * (which is the case for manually constructed entities).
 *
 * Supports profile/non-profile RawContacts with native/custom data.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be
 * returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun DataEntity.rawContact(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): RawContact? = rawContactId?.let { rawContactId ->
    contacts.findFirstRawContactWithId(rawContactId, cancel)
}