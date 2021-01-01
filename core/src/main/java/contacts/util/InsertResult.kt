package contacts.util

import android.content.Context
import contacts.*
import contacts.custom.CustomCommonDataRegistry
import contacts.custom.GlobalCustomCommonDataRegistry
import contacts.entities.Contact
import contacts.entities.MutableRawContact
import contacts.entities.RawContact

/**
 * Returns the newly created [RawContact] or null if the insert operation failed.
 *
 * ## Permissions
 *
 * The [contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be returned
 * if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Insert.Result.rawContact(
    context: Context,
    rawContact: MutableRawContact,
    customDataRegistry: CustomCommonDataRegistry = GlobalCustomCommonDataRegistry,
    cancel: () -> Boolean = { false }
): RawContact? {

    val rawContactId = rawContactId(rawContact) ?: return null

    return Query(context, customDataRegistry).where(Fields.RawContact.Id equalTo rawContactId)
        .find(cancel)
        .asSequence()
        .flatMap { it.rawContacts.asSequence() }
        .find { it.id == rawContactId }
}

/**
 * Returns all newly created [RawContact]s (for those insert operations that succeeded).
 *
 * ## Permissions
 *
 * The [contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, an empty list will be
 * returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Insert.Result.rawContacts(
    context: Context,
    customDataRegistry: CustomCommonDataRegistry = GlobalCustomCommonDataRegistry,
    cancel: () -> Boolean = { false }
): List<RawContact> =
    Query(context, customDataRegistry).where(Fields.RawContact.Id `in` rawContactIds).find(cancel)
        .asSequence()
        .flatMap { it.rawContacts.asSequence() }
        .filter { rawContactIds.contains(it.id) }
        .toList()

/**
 * Returns the newly created [Contact] containing the [RawContact] or null if the insert operation
 * failed.
 *
 * ## Permissions
 *
 * The [contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be returned
 * if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Insert.Result.contact(
    context: Context,
    rawContact: MutableRawContact,
    customDataRegistry: CustomCommonDataRegistry = GlobalCustomCommonDataRegistry,
    cancel: () -> Boolean = { false }
): Contact? {

    val rawContactId = rawContactId(rawContact) ?: return null

    return Query(context, customDataRegistry)
        .where(Fields.RawContact.Id equalTo rawContactId)
        .find(cancel)
        .firstOrNull()
}

/**
 * Returns all newly created [Contact]s containing the [RawContact]s (for those insert operations
 * that succeeded).
 *
 * Returns an empty list all insert operations failed.
 *
 * ## Permissions
 *
 * The [contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, an empty list will be
 * returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Insert.Result.contacts(
    context: Context,
    customDataRegistry: CustomCommonDataRegistry = GlobalCustomCommonDataRegistry,
    cancel: () -> Boolean = { false }
): List<Contact> =
    Query(context, customDataRegistry).where(Fields.RawContact.Id `in` rawContactIds).find(cancel)