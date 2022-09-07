package contacts.core.util

import contacts.core.Contacts
import contacts.core.entities.NewSimContact
import contacts.core.entities.SimContact
import contacts.core.sim.SimContactsInsert

/**
 * Returns the newly created [SimContact].
 *
 * Returns null if the insert operation failed or permissions are not granted.
 *
 * ## Duplicate entries
 *
 * Due to duplicate entries being allowed and row IDs not being reliable, this may return a non-null
 * value even if the insert actually failed if the same (duplicate) [simContact] already exist
 * before this insert operation. In other words, there is no sure way of knowing if creating
 * duplicates actually succeeded or not.
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
fun SimContactsInsert.Result.simContact(
    contacts: Contacts, simContact: NewSimContact, cancel: () -> Boolean = { false }
): SimContact? = if (isSuccessful(simContact)) {
    contacts.sim().query().find(cancel)
        // Attempt to get the reference to the most recently inserted entry by getting the "last" one.
        .lastOrNull { it.name == simContact.name && it.number == simContact.number }
} else {
    null
}

/**
 * Returns the newly created [SimContact]s (for those insert operations that succeeded).
 *
 * Returns an empty list if the insert operation failed or permissions are not granted.
 *
 * ## Duplicate entries
 *
 * Due to duplicate entries being allowed and row IDs not being reliable, this may return non-null
 * values even if the insert actually failed if the same (duplicate) [simContact]s already exist
 * before this insert operation. In other words, there is no sure way of knowing if creating
 * duplicates actually succeeded or not.
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
fun SimContactsInsert.Result.simContacts(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): List<SimContact> {
    val existingSimContacts = contacts.sim().query().find(cancel)

    return newSimContacts.mapNotNull { newSimContact ->
        // Double for-loop but it should be fine unless the number of new AND existing contacts
        // are large. Attempt to get the reference to the most recently inserted entry by getting
        // the "last" one.
        existingSimContacts.lastOrNull { existingSimContact ->
            newSimContact.name == existingSimContact.name &&
                    newSimContact.number == existingSimContact.number
        }
    }
}