package contacts.core.util

import contacts.core.Contacts
import contacts.core.Fields
import contacts.core.entities.Contact
import contacts.core.entities.MutableContact
import contacts.core.equalTo

/**
 * Returns the contact with all of the latest data, including all
 * [contacts.core.entities.RawContact]s.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the [Contact] no longer exists or if permission is not granted.
 *
 * Returns itself if the [Contact.id] is null, indicating that this Contact instance has not yet
 * been inserted to the DB.
 *
 * Supports profile and non-profile Contacts with native/custom data.
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
fun Contact.refresh(contacts: Contacts, cancel: () -> Boolean = { false }): Contact? =
    if (id == null) {
        this
    } else {
        contacts.findFirstContactWithId(id, cancel)
    }

/**
 * See [Contact.refresh].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun MutableContact.refresh(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): MutableContact? =
    if (id == null) {
        this
    } else {
        contacts.findFirstContactWithId(id, cancel)?.toMutableContact()
    }

internal fun Contacts.findFirstContactWithId(
    contactId: Long,
    cancel: () -> Boolean
): Contact? =
    if (contactId.isProfileId) {
        profile()
            .query()
            .find(cancel)
    } else {
        query()
            .where(Fields.Contact.Id equalTo contactId)
            .find(cancel)
            .firstOrNull()
    }