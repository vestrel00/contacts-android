package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Query
import com.vestrel00.contacts.`in`
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.equalTo

// Note that there is no need to handle isProfile here as ContactLinks operations do not support it.

/**
 * Returns the [Contact] that contains all of the successfully linked RawContacts or null if the
 * link operation failed.
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
fun ContactLinkResult.contact(context: Context, cancel: () -> Boolean = { false }): Contact? =
    contactId?.let {
        Query(context)
            .where(Fields.Contact.Id equalTo it)
            .find(cancel)
            .firstOrNull()
    }

/**
 * Returns all of the [Contact]s that are associated with each of the unlinked RawContacts.
 * Returns an empty list if the unlink operation failed.
 *
 * ## Permissions
 *
 * The [com.vestrel00.contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, empty
 * list will be returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun ContactUnlinkResult.contacts(context: Context, cancel: () -> Boolean = { false }):
        List<Contact> = if (rawContactIds.isEmpty()) {
    emptyList()
} else {
    Query(context).where(Fields.RawContact.Id `in` rawContactIds).find(cancel)
}

