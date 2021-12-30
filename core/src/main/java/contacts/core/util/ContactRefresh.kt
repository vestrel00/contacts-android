package contacts.core.util

import contacts.core.Contacts
import contacts.core.ContactsException
import contacts.core.entities.Contact
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.MutableContact
import contacts.core.equalTo

/**
 * Returns the contact with all of the latest data, including all RawContacts.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the Contact no longer exists or if permission is not granted.
 *
 * Supports profile and non-profile Contacts with native/custom data.
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
fun <T : ExistingContactEntity> T.refresh(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): T? {
    val contact = contacts.findContactWithId(id, cancel)

    @Suppress("UNCHECKED_CAST")
    return when (this) {
        is Contact -> contact
        is MutableContact -> contact?.mutableCopy()
        // This else is required because we are using the generic type T as the receiver. As of
        // Kotlin 1.6, this else is required. Using reified for T does not work (even if it did,
        // we'd have to inline the function, which is not Java-friendly). Changing the receiver
        // to ExistingContactEntity instead of T removes the need for the else block.
        else -> throw ContactsException(
            "Unrecognized ExistingContactEntity: ${this.javaClass.simpleName}"
        )
    } as T?
}

internal fun Contacts.findContactWithId(
    contactId: Long,
    cancel: () -> Boolean
): Contact? =
    if (contactId.isProfileId) {
        // Remember there is only one profile Contact so there is no need to look for ID.
        profile()
            .query()
            .find(cancel)
            .contact
    } else {
        query()
            .where { Contact.Id equalTo contactId }
            .find(cancel)
            .firstOrNull()
    }