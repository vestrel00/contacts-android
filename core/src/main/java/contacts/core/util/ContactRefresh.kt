package contacts.core.util

import contacts.core.Contacts
import contacts.core.LookupQuery
import contacts.core.entities.Contact
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.MutableContact
import contacts.core.equalTo

/**
 * Returns the contact with all of the latest data, including all RawContacts.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the Contact no longer exists or if permission is not granted.
 *
 * Supports profile and non-profile Contacts with native/custom data.
 *
 * ## Contact linking/unlinking and changing Accounts
 *
 * Note that this uses the [ExistingContactEntity.lookupKey] (**if available**) and the
 * [ExistingContactEntity.id] to fetch the Contact.
 *
 * You may use this function to refresh a Contact reference that you are holding on to for longer
 * periods of time. For example,
 *
 * - Creating and loading shortcuts.
 * - Saving/restoring activity/fragment instance state.
 * - Saving to an external database, preferences, or files.
 *
 * The ID or lookup key (for local contacts) may change due to sync or aggregation. However, this
 * function will still return the correct Contact.
 *
 * If the **lookupKey of this instance is null**, then it was probably not included in the query. In
 * this case, **only the ID will be used, which is not as reliable as the lookup key!**.
 *
 * If the Contact's constituent RawContact(s) changes Accounts, this may return null. This is the
 * same behavior as the AOSP and Google Contacts app.
 *
 * If this is a reference to a Contact with two or more constituent RawContacts
 * and the Contact has been unlinked (thereby creating separate Contact instances), this will only
 * return one of the Contacts it finds instead of all of the previously linked contacts.
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
fun <T : ExistingContactEntity> T.refresh(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): T? {
    val contact = contacts.findContactWithLookupKeyOrId(lookupKey, id, cancel)

    @Suppress("UNCHECKED_CAST")
    return when (this) {
        is Contact -> contact
        is MutableContact -> contact?.mutableCopy()
    } as T?
}

internal fun Contacts.findContactWithId(
    contactId: Long,
    cancel: () -> Boolean
): Contact? = findContactWithLookupKeyOrId(null, contactId, cancel)

private fun Contacts.findContactWithLookupKeyOrId(
    lookupKey: String?,
    contactId: Long,
    cancel: () -> Boolean
): Contact? = if (contactId.isProfileId) {
    // Remember there is only one profile Contact.
    profile()
        .query()
        .find(cancel)
        .contact
} else {
    if (lookupKey.isNullOrBlank()) {
        query()
            .where { Contact.Id equalTo contactId }
            .find(cancel)
            .firstOrNull()
    } else {
        lookupQuery()
            .whereLookupKeyWithIdMatches(LookupQuery.LookupKeyWithId(lookupKey, contactId))
            .find(cancel)
            .firstOrNull()
    }
}