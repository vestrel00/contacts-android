package contacts.core.util

import contacts.core.Contacts
import contacts.core.entities.CommonDataEntity
import contacts.core.entities.Contact

/**
 * Returns the [Contact] with the [CommonDataEntity.contactId].
 *
 * This may return null if the [Contact] no longer exists or if [CommonDataEntity.contactId] is null
 * (which is the case for manually constructed entities).
 *
 * Supports profile/non-profile Contacts with native/custom data.
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
fun CommonDataEntity.contact(contacts: Contacts, cancel: () -> Boolean = { false }): Contact? =
    contactId?.let { contactId ->
        contacts.findFirstContactWithId(contactId, cancel)
    }