package contacts.core.util

import contacts.core.Contacts
import contacts.core.Insert
import contacts.core.`in`
import contacts.core.entities.Contact
import contacts.core.entities.NewRawContact
import contacts.core.entities.RawContact
import contacts.core.equalTo

/**
 * Returns the newly created [RawContact] or null if the insert operation failed.
 *
 * Supports RawContacts with native/custom data.
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
fun Insert.Result.rawContact(
    contacts: Contacts,
    rawContact: NewRawContact,
    cancel: () -> Boolean = { false }
): RawContact? {

    val rawContactId = rawContactId(rawContact) ?: return null

    return contacts.query()
        .where { RawContact.Id equalTo rawContactId }
        .find(cancel)
        .asSequence()
        .flatMap { it.rawContacts.asSequence() }
        .find { it.id == rawContactId }
}

/**
 * Returns all newly created [RawContact]s (for those insert operations that succeeded).
 *
 * Supports RawContacts with native/custom data.
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
fun Insert.Result.rawContacts(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): List<RawContact> =
    contacts.query().where { RawContact.Id `in` rawContactIds }
        .find(cancel)
        .asSequence()
        .flatMap { it.rawContacts.asSequence() }
        .filter { rawContactIds.contains(it.id) }
        .toList()

/**
 * Returns the newly created [Contact] containing the [RawContact] or null if the insert operation
 * failed.
 *
 * Supports Contacts with native/custom data.
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
fun Insert.Result.contact(
    contacts: Contacts,
    rawContact: NewRawContact,
    cancel: () -> Boolean = { false }
): Contact? {

    val rawContactId = rawContactId(rawContact) ?: return null

    return contacts.query()
        .where { RawContact.Id equalTo rawContactId }
        .find(cancel)
        .firstOrNull()
}

/**
 * Returns all newly created [Contact]s containing the [RawContact]s (for those insert operations
 * that succeeded).
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
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Insert.Result.contacts(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): List<Contact> =
    contacts.query()
        .where { RawContact.Id `in` rawContactIds }
        .find(cancel)