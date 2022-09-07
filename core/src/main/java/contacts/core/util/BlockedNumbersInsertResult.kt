package contacts.core.util

import contacts.core.Contacts
import contacts.core.`in`
import contacts.core.blockednumbers.BlockedNumbersInsert
import contacts.core.blockednumbers.BlockedNumbersPrivileges
import contacts.core.entities.BlockedNumber
import contacts.core.entities.NewBlockedNumber
import contacts.core.equalTo

/**
 * Returns the newly created [BlockedNumber].
 *
 * Returns null if the insert operation failed or privileges are not acquired.
 *
 * ## Privileges
 *
 * Requires [BlockedNumbersPrivileges.canReadAndWrite].
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
fun BlockedNumbersInsert.Result.blockedNumber(
    contacts: Contacts, blockedNumber: NewBlockedNumber, cancel: () -> Boolean = { false }
): BlockedNumber? = blockedNumberId(blockedNumber)?.let { blockedNumberId ->
    contacts.blockedNumbers().query()
        .where { Id equalTo blockedNumberId }
        .find(cancel)
        .firstOrNull()
}

/**
 * Returns the newly created [BlockedNumber]s (for those insert operations that succeeded).
 *
 * Returns an empty list if the insert operation failed or privileges are not acquired.
 *
 * ## Privileges
 *
 * Requires [BlockedNumbersPrivileges.canReadAndWrite].
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
fun BlockedNumbersInsert.Result.blockedNumbers(
    contacts: Contacts,
    cancel: () -> Boolean = { false }
): List<BlockedNumber> =
    if (blockedNumberIds.isEmpty()) {
        emptyList()
    } else {
        contacts.blockedNumbers().query()
            .where { Id `in` blockedNumberIds }
            .find(cancel)
    }