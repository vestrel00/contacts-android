package contacts.core.util

import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.RawContact
import contacts.core.profile.ProfileInsert

/**
 * Returns the newly created Profile [RawContact] or null if the insert operation failed.
 *
 * Supports RawContacts with native/custom data.
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
fun ProfileInsert.Result.rawContact(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): RawContact? = rawContactId?.let { rawContactId ->
    contact(contacts, cancel)
        ?.rawContacts
        ?.firstOrNull { it.id == rawContactId }
}

/**
 * Returns the Profile [Contact] containing the newly created [RawContact] or null if the insert
 * operation failed.
 *
 * Supports Contacts with native/custom data.
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
fun ProfileInsert.Result.contact(contacts: Contacts, cancel: () -> Boolean = { false }): Contact? =
    if (isSuccessful) contacts.profile().query().find(cancel).contact else null