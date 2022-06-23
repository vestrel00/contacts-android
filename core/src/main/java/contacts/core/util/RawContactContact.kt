package contacts.core.util

import contacts.core.*
import contacts.core.entities.Contact
import contacts.core.entities.ExistingRawContactEntityWithContactId
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table

/**
 * Returns the [Contact] with the [ExistingRawContactEntityWithContactId.contactId].
 *
 * This may return null if the Contact or RawContact no longer exists or if permissions are not
 * granted.
 *
 * Supports profile and non-profile Contacts with native/custom data.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required.
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
fun ExistingRawContactEntityWithContactId.contact(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): Contact? = contacts.getContactIdFromRawContactsTable(id)?.let { contactIdFromDb ->
    // Note that we do not need to use the Contact lookup key because we are fetching the latest
    // Contact ID value from database anyways. Lookup by ID (a number) is faster than lookup by
    // lookup key (String/Text).
    contacts.findContactWithId(contactIdFromDb, cancel)
}

private fun Contacts.getContactIdFromRawContactsTable(rawContactId: Long): Long? =
    contentResolver.query(
        if (rawContactId.isProfileId) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
        Include(RawContactsFields.ContactId),
        RawContactsFields.Id equalTo rawContactId
    ) {
        it.getNextOrNull { it.rawContactsCursor().contactId }
    }