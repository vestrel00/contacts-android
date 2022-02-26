package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.blockednumbers.BlockedNumbersInsert
import contacts.core.entities.BlockedNumber
import contacts.core.entities.NewBlockedNumber
import contacts.core.util.blockedNumber
import contacts.core.util.blockedNumbers
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlockedNumbersInsert.Result.blockedNumber].
 */
suspend fun BlockedNumbersInsert.Result.blockedNumberWithContext(
    contacts: Contacts,
    blockedNumber: NewBlockedNumber,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): BlockedNumber? =
    withContext(coroutineContext) { blockedNumber(contacts, blockedNumber) { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlockedNumbersInsert.Result.blockedNumbers].
 */
suspend fun BlockedNumbersInsert.Result.blockedNumbersWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<BlockedNumber> = withContext(coroutineContext) { blockedNumbers(contacts) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlockedNumbersInsert.Result.blockedNumber].
 */
fun BlockedNumbersInsert.Result.blockedNumberAsync(
    contacts: Contacts,
    blockedNumber: NewBlockedNumber,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<BlockedNumber?> =
    CoroutineScope(coroutineContext).async { blockedNumber(contacts, blockedNumber) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlockedNumbersInsert.Result.blockedNumbers].
 */
fun BlockedNumbersInsert.Result.blockedNumbersAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<BlockedNumber>> =
    CoroutineScope(coroutineContext).async { blockedNumbers(contacts) { !isActive } }