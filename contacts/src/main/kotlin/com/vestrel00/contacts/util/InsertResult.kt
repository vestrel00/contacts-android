package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact

/**
 * Returns the newly created [RawContact] or null if the insert operation failed.
 *
 * ## Permissions
 *
 * The [com.vestrel00.contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null
 * will be returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Insert.Result.rawContact(
    context: Context, rawContact: MutableRawContact, cancel: () -> Boolean = { false }
): RawContact? {

    val rawContactId = rawContactId(rawContact) ?: return null

    return Query(context).where(Fields.RawContactId equalTo rawContactId).find(cancel)
        .asSequence()
        .flatMap { it.rawContacts.asSequence() }
        .find { it.id == rawContactId }
}

/**
 * Returns all newly created [RawContact]s (for those insert operations that succeeded).
 *
 * ## Permissions
 *
 * The [com.vestrel00.contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, an empty
 * list will be returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Insert.Result.rawContacts(
    context: Context, cancel: () -> Boolean = { false }
): List<RawContact> = Query(context).where(Fields.RawContactId `in` rawContactIds).find(cancel)
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
 * The [com.vestrel00.contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null
 * will be returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Insert.Result.contact(
    context: Context, rawContact: MutableRawContact, cancel: () -> Boolean = { false }
): Contact? {

    val rawContactId = rawContactId(rawContact) ?: return null

    return Query(context).where(Fields.RawContactId equalTo rawContactId).findFirst(cancel)
}

/**
 * Returns all newly created [Contact]s containing the [RawContact]s (for those insert operations
 * that succeeded).
 *
 * Returns an empty list all insert operations failed.
 *
 * ## Permissions
 *
 * The [com.vestrel00.contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, an empty
 * list will be returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Insert.Result.contacts(context: Context, cancel: () -> Boolean = { false }): List<Contact> =
    Query(context).where(Fields.RawContactId `in` rawContactIds).find(cancel)