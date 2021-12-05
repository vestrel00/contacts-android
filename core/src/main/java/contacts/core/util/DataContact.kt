package contacts.core.util

import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.DataEntity

/**
 * Returns the [Contact] with the [DataEntity.contactId].
 *
 * This may return null if the [Contact] no longer exists or if [DataEntity.contactId] is null
 * (which is the case for manually constructed entities).
 *
 * Supports profile/non-profile Contacts with native/custom data.
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
fun DataEntity.contact(contacts: Contacts, cancel: () -> Boolean = { false }): Contact? =
    contactId?.let { contactId ->
        contacts.findContactWithId(contactId, cancel)
    }