package contacts.core.util

import contacts.core.Contacts
import contacts.core.Fields
import contacts.core.entities.Contact
import contacts.core.entities.MutableContact
import contacts.core.equalTo

/**
 * Returns the contact with all of the latest data, including all
 * [contacts.core.entities.RawContact]s.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the [Contact] no longer exists or if permission is not granted.
 *
 * Returns itself if the [Contact.id] is null, indicating that this Contact instance has not yet
 * been inserted to the DB.
 *
 * Supports profile and non-profile Contacts with native/custom data.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be returned
 * if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Contact.refresh(contacts: Contacts, cancel: () -> Boolean = { false }): Contact? =
    if (id == null) {
        this
    } else {
        contacts.findFirstContactWithId(id, cancel)
    }

/**
 * See [Contact.refresh].
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun MutableContact.refresh(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): MutableContact? =
    if (id == null) {
        this
    } else {
        contacts.findFirstContactWithId(id, cancel)?.mutableCopy()
    }

internal fun Contacts.findFirstContactWithId(
    contactId: Long,
    cancel: () -> Boolean
): Contact? =
    if (contactId.isProfileId) {
        profile()
            .query()
            .find(cancel)
    } else {
        query()
            .where(Fields.Contact.Id equalTo contactId)
            .find(cancel)
            .firstOrNull()
    }

/* DEV NOTE
We could declare and implement a single function instead of two by using the generic type...
fun <T: ContactEntity> T.refresh(contacts: Contacts, cancel: () -> Boolean = { false }): T? =
However, unsafe type casting is required, which I'd rather avoid =)

As of Kotlin 1.6.0...

sealed interface Human
class Male : Human
class Female : Human

// This is clean but does not return concrete type.
fun Human.refresh(): Human = when (this) {
    is Male -> Male()
    is Female -> Female()
}

// This returns concrete type but requires unchecked cast. Also the when statement asks for an else
// branch even though the interface is sealed...
@Suppress("UNCHECKED_CAST")
fun <T : Human> T.refresh(): T = when (this) {
    is Male -> Male()
    is Female -> Female()
    else -> throw UnknownHumanException()
} as T

// Inlining to use reified does not help. Plus, this is not Java-friendly =(
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Human> T.refresh(): T = when (T::class) {
    Male::class -> Male()
    Female::class -> Female()
    else -> throw UnknownHumanException()
} as T

So... we will keep things this way until Kotlin supports the following code (if ever),
fun <T : Human> T.refresh(): T = when (this) {
    is Male -> Male()
    is Female -> Female()
}
*/