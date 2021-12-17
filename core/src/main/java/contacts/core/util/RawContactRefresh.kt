package contacts.core.util

import contacts.core.Contacts
import contacts.core.ContactsException
import contacts.core.Fields
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
        // This else is required because we are using the generic type T as the receiver. As of
        // Kotlin 1.6, this else is required. Using reified for T does not work (even if it did,
        // we'd have to inline the function, which is not Java-friendly). Changing the receiver
        // to ExistingRawContactEntity instead of T removes the need for the else block.
        else -> throw ContactsException(
            "Unrecognized ExistingRawContactEntity: ${this.javaClass.simpleName}"
        )
    } as T?
}

internal fun Contacts.findRawContactWithId(
    rawContactId: Long,
    cancel: () -> Boolean
): RawContact? = if (rawContactId.isProfileId) {
    profile().query()
        .find(cancel)
} else {
    query()
        .where(Fields.RawContact.Id equalTo rawContactId)
        .find(cancel)
        .firstOrNull()
}
    ?.rawContacts
    ?.find { it.id == rawContactId }