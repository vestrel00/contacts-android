package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.Insert
import contacts.core.entities.Contact
import contacts.core.entities.MutableRawContact
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
 * See [Insert.Result.rawContact].
 */
suspend fun Insert.Result.rawContactWithContext(
    contacts: Contacts,
    rawContact: MutableRawContact,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): RawContact? = withContext(coroutineContext) {
    rawContact(contacts, rawContact) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.rawContacts].
 */
suspend fun Insert.Result.rawContactsWithContext(
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
 * See [Insert.Result.contact].
 */
suspend fun Insert.Result.contactWithContext(
    contacts: Contacts,
    rawContact: MutableRawContact,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) {
    contact(contacts, rawContact) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.contacts].
 */
suspend fun Insert.Result.contactsWithContext(
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
 * See [Insert.Result.rawContact].
 */
fun Insert.Result.rawContactAsync(
    contacts: Contacts,
    rawContact: MutableRawContact,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<RawContact?> = CoroutineScope(coroutineContext).async {
    rawContact(contacts, rawContact) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.rawContacts].
 */
fun Insert.Result.rawContactsAsync(
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
 * See [Insert.Result.contact].
 */
fun Insert.Result.contactAsync(
    contacts: Contacts,
    rawContact: MutableRawContact,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> = CoroutineScope(coroutineContext).async {
    contact(contacts, rawContact) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.contacts].
 */
fun Insert.Result.contactsAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<Contact>> = CoroutineScope(coroutineContext).async {
    contacts(contacts) { !isActive }
}

// endregion