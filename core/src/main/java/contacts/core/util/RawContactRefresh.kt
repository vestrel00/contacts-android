package contacts.core.util

import contacts.core.Contacts
import contacts.core.RawContactsFields
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact
import contacts.core.equalTo

/**
 * Returns the [ExistingRawContactEntity] [T] with all of the latest data.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the RawContact no longer exists or if permission is not granted.
 *
 * Supports profile and non-profile RawContacts with native/custom data.
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
fun <T : ExistingRawContactEntity> T.refresh(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): T? {
    val rawContact = contacts.findRawContactWithId(id, cancel)

    @Suppress("UNCHECKED_CAST")
    return when (this) {
        is RawContact -> rawContact
        is MutableRawContact -> rawContact?.mutableCopy()
    } as T?
}

internal fun Contacts.findRawContactWithId(
    rawContactId: Long,
    cancel: () -> Boolean
): RawContact? = if (rawContactId.isProfileId) {
    profile().rawContactsQuery()
} else {
    rawContactsQuery()
}
    .rawContactsWhere(emptyList(), RawContactsFields.Id equalTo rawContactId)
    .find(cancel)
    .firstOrNull()