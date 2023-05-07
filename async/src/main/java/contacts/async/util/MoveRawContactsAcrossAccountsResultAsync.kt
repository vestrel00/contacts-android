package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.accounts.MoveRawContactsAcrossAccounts
import contacts.core.entities.Contact
import contacts.core.entities.RawContact
import contacts.core.util.contact
import contacts.core.util.contacts
import contacts.core.util.rawContact
import contacts.core.util.rawContacts
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MoveRawContactsAcrossAccounts.Result.rawContact].
 */
suspend fun MoveRawContactsAcrossAccounts.Result.rawContactWithContext(
    contacts: Contacts,
    originalRawContactId: Long,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): RawContact? = withContext(coroutineContext) {
    rawContact(contacts, originalRawContactId) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MoveRawContactsAcrossAccounts.Result.rawContacts].
 */
suspend fun MoveRawContactsAcrossAccounts.Result.rawContactsWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<RawContact> = withContext(coroutineContext) {
    rawContacts(contacts) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MoveRawContactsAcrossAccounts.Result.contact].
 */
suspend fun MoveRawContactsAcrossAccounts.Result.contactWithContext(
    contacts: Contacts,
    originalRawContactId: Long,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) {
    contact(contacts, originalRawContactId) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MoveRawContactsAcrossAccounts.Result.contacts].
 */
suspend fun MoveRawContactsAcrossAccounts.Result.contactsWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<Contact> = withContext(coroutineContext) {
    contacts(contacts) { !isActive }
}

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MoveRawContactsAcrossAccounts.Result.rawContact].
 */
fun MoveRawContactsAcrossAccounts.Result.rawContactAsync(
    contacts: Contacts,
    originalRawContactId: Long,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<RawContact?> = CoroutineScope(coroutineContext).async {
    rawContact(contacts, originalRawContactId) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MoveRawContactsAcrossAccounts.Result.rawContacts].
 */
fun MoveRawContactsAcrossAccounts.Result.rawContactsAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<RawContact>> = CoroutineScope(coroutineContext).async {
    rawContacts(contacts) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MoveRawContactsAcrossAccounts.Result.contact].
 */
fun MoveRawContactsAcrossAccounts.Result.contactAsync(
    contacts: Contacts,
    originalRawContactId: Long,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> = CoroutineScope(coroutineContext).async {
    contact(contacts, originalRawContactId) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MoveRawContactsAcrossAccounts.Result.contacts].
 */
fun MoveRawContactsAcrossAccounts.Result.contactsAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<Contact>> = CoroutineScope(coroutineContext).async {
    contacts(contacts) { !isActive }
}

// endregion