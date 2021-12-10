package contacts.core.util

import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.RawContactEntity

/**
 * Returns the [Contact] with the [RawContactEntity.contactId].
 *
 * This may return null if the [Contact] no longer exists or if permissions are not granted.
 *
 * Supports profile and non-profile Contacts with native/custom data.
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
fun RawContactEntity.contact(contacts: Contacts, cancel: () -> Boolean = { false }): Contact? =
    contactId?.let { contactId ->
        contacts.findContactWithId(contactId, cancel)
    }