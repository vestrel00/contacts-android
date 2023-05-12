package contacts.core.util

import contacts.core.Contacts
import contacts.core.accounts.MoveRawContactsAcrossAccounts
import contacts.core.entities.Contact
import contacts.core.entities.RawContact
import contacts.core.equalTo
import contacts.core.`in`

/**
 * Returns the newly created [RawContact] or null if the move insert operation failed.
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
fun MoveRawContactsAcrossAccounts.Result.rawContact(
    contacts: Contacts,
    originalRawContactId: Long,
    cancel: () -> Boolean = { false }
): RawContact? = rawContactId(originalRawContactId)?.let { rawContactId ->
    contacts
        .rawContactsQuery()
        .where { RawContact.Id equalTo rawContactId }
        .find(cancel)
        .find { it.id == rawContactId }
}

/**
 * Returns all newly created [RawContact]s (for those move insert operations that succeeded).
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
fun MoveRawContactsAcrossAccounts.Result.rawContacts(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): List<RawContact> = contacts
    .rawContactsQuery()
    .where { RawContact.Id `in` rawContactIds }
    .find(cancel)

/**
 * Returns the newly created [Contact] containing the [RawContact] or null if the move insert
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
fun MoveRawContactsAcrossAccounts.Result.contact(
    contacts: Contacts,
    originalRawContactId: Long,
    cancel: () -> Boolean = { false }
): Contact? = rawContactId(originalRawContactId)?.let { rawContactId ->
    contacts.query()
        .where { RawContact.Id equalTo rawContactId }
        .find(cancel)
        .firstOrNull()
}

/**
 * Returns all newly created [Contact]s containing the [RawContact]s (for those move insert
 * operations that succeeded).
 *
 * Supports Contacts with native/custom data.
 *
 * Returns an empty list all insert operations failed.
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
fun MoveRawContactsAcrossAccounts.Result.contacts(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): List<Contact> = contacts.query()
    .where { RawContact.Id `in` rawContactIds }
    .find(cancel)