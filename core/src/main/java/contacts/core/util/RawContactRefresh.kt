package contacts.core.util

import contacts.core.Contacts
import contacts.core.Fields
import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact
import contacts.core.equalTo

/**
 * Returns the [RawContact] with all of the latest data.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the [RawContact] no longer exists or if permission is not granted.
 *
 * Returns itself if the [RawContact.id] is null, indicating that this RawContact instance has not
 * yet been inserted to the DB.
 *
 * Supports profile and non-profile RawContacts with native/custom data.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be
 * returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun RawContact.refresh(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): RawContact? =
    if (id == null) {
        this
    } else {
        contacts.findRawContactWithId(id, cancel)
    }

/**
 * See [RawContact.refresh].
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
fun MutableRawContact.refresh(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): MutableRawContact? = if (id == null) {
    this
} else {
    contacts.findRawContactWithId(id, cancel)?.mutableCopy()
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