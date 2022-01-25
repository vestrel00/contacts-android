package contacts.core.util

import contacts.core.*
import contacts.core.entities.Contact
import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.cursor.dataCursor
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table

/**
 * Returns the [Contact] with the [ExistingDataEntity.contactId].
 *
 * This may return null if the [Contact] no longer exists or if permissions are not granted.
 *
 * Supports profile/non-profile Contacts with native/custom data.
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
fun ExistingDataEntity.contact(contacts: Contacts, cancel: () -> Boolean = { false }): Contact? =
    contacts.getContactIdFromDataTable(id)?.let { contactIdFromDb ->
        // Note that we do not need to use the Contact lookup key because we are fetching the latest
        // Contact ID value from database anyways. Lookup by ID (a number) is faster than lookup by
        // lookup key (String/Text).
        contacts.findContactWithId(contactIdFromDb, cancel)
    }

private fun Contacts.getContactIdFromDataTable(dataId: Long): Long? = contentResolver.query(
    if (dataId.isProfileId) ProfileUris.DATA.uri else Table.Data.uri,
    Include(Fields.Contact.Id),
    Fields.DataId equalTo dataId
) {
    it.getNextOrNull { it.dataCursor().contactId }
}