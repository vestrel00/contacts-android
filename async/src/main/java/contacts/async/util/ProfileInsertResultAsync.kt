package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.RawContact
import contacts.core.profile.ProfileInsert
import contacts.core.util.contact
import contacts.core.util.rawContact
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.rawContact].
 */
suspend fun ProfileInsert.Result.rawContactWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): RawContact? = withContext(coroutineContext) {
    rawContact(contacts) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.contact].
 */
suspend fun ProfileInsert.Result.contactWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) {
    contact(contacts) { !isActive }
}

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.rawContact].
 */
fun ProfileInsert.Result.rawContactAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<RawContact?> = CoroutineScope(coroutineContext).async {
    rawContact(contacts) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.contact].
 */
fun ProfileInsert.Result.contactAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> = CoroutineScope(coroutineContext).async {
    contact(contacts) { !isActive }
}

// endregion