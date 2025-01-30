package contacts.core.util

import contacts.core.Contacts
import contacts.core.Include
import contacts.core.RawContactsFields
import contacts.core.contentResolver
import contacts.core.entities.Contact
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.equalTo

/**
 * Returns the [Contact] with the [ExistingRawContactEntity.contactId].
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
fun ExistingRawContactEntity.contact(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): Contact? = contacts.getContactIdFromRawContactsTable(id)?.let { contactIdFromDb ->
    contacts.findContactWithId(contactIdFromDb, cancel)
}

private fun Contacts.getContactIdFromRawContactsTable(rawContactId: Long): Long? =
    contentResolver.query(
        rawContactsUri(isProfile = rawContactId.isProfileId),
        Include(RawContactsFields.ContactId),
        RawContactsFields.Id equalTo rawContactId
    ) {
        it.getNextOrNull { it.rawContactsCursor().contactId }
    }