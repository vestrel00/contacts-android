package contacts.core.util

import contacts.core.Contacts
import contacts.core.Fields
import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact
import contacts.core.equalTo

/**
 * Returns the [RawContact] with all of the latest data.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the [RawContact] no longer exists or if permission is not granted.
 *
 * Returns itself if the [RawContact.id] is null, indicating that this RawContact instance has not
 * yet been inserted to the DB.
 *
 * Supports profile and non-profile RawContacts with native/custom data.
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
fun RawContact.refresh(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): RawContact? =
    if (id == null) {
        this
    } else {
        contacts.findFirstRawContactWithId(id, cancel)
    }

/**
 * See [RawContact.refresh].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun MutableRawContact.refresh(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): MutableRawContact? = if (id == null) {
    this
} else {
    contacts.findFirstRawContactWithId(id, cancel)?.toMutableRawContact()
}

internal fun Contacts.findFirstRawContactWithId(
    rawContactId: Long,
    cancel: () -> Boolean
): RawContact? = if (rawContactId.isProfileId) {
    profile().query()
        .find(cancel)
        ?.rawContacts
        ?.find { it.id == rawContactId }
} else {
    query()
        .where(Fields.RawContact.Id equalTo rawContactId)
        .find(cancel)
        .firstOrNull()
        ?.rawContacts
        ?.firstOrNull()
}