package contacts.core.util

import contacts.core.Contacts
import contacts.core.Fields
import contacts.core.entities.Contact
import contacts.core.entities.ExistingDataEntity
import contacts.core.equalTo

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
    contacts.getContactIdFromDataTable(id, cancel)?.let { contactIdFromDb ->
        contacts.findContactWithId(contactIdFromDb, cancel)
    }

private fun Contacts.getContactIdFromDataTable(
    dataId: Long,
    cancel: () -> Boolean
): Long? = if (dataId.isProfileId) {
    // Remember there is only one profile Contact. We don't really need to perform this query
    // because even if the Profile Contact ID changed due to sync or aggregation, the ID will
    // still be a profile ID. The query in which the value this returns will still be correct.
    // This query is only done for documentation / OCD purposes only...
    profile()
        .query()
        .include(Fields.Contact.Id)
        .find(cancel)
        .contact?.id
} else {
    query()
        .include(Fields.Contact.Id)
        .where { DataId equalTo dataId }
        .find(cancel)
        .firstOrNull()
        ?.id
}