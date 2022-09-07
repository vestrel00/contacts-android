package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.NewSimContact
import contacts.core.entities.SimContact
import contacts.core.sim.SimContactsInsert
import contacts.core.util.simContact
import contacts.core.util.simContacts
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsInsert.Result.simContact].
 */
suspend fun SimContactsInsert.Result.simContactWithContext(
    contacts: Contacts,
    simContact: NewSimContact,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): SimContact? =
    withContext(coroutineContext) { simContact(contacts, simContact) { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsInsert.Result.simContacts].
 */
suspend fun SimContactsInsert.Result.simContactsWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<SimContact> = withContext(coroutineContext) { simContacts(contacts) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsInsert.Result.simContact].
 */
fun SimContactsInsert.Result.simContactAsync(
    contacts: Contacts,
    simContact: NewSimContact,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<SimContact?> =
    CoroutineScope(coroutineContext).async { simContact(contacts, simContact) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsInsert.Result.simContacts].
 */
fun SimContactsInsert.Result.simContactsAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<SimContact>> =
    CoroutineScope(coroutineContext).async { simContacts(contacts) { !isActive } }