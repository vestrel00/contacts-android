package contacts.async.sim

import contacts.async.ASYNC_DISPATCHER
import contacts.core.sim.SimCardMaxCharacterLimits
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimCardMaxCharacterLimits.nameMaxLength].
 */
suspend fun SimCardMaxCharacterLimits.nameMaxLengthWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): Int = withContext(context) { nameMaxLength { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimCardMaxCharacterLimits.numberMaxLength].
 */
suspend fun SimCardMaxCharacterLimits.numberMaxLengthWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): Int = withContext(context) { numberMaxLength { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimCardMaxCharacterLimits.numberMaxLength].
 */
fun SimCardMaxCharacterLimits.numberMaxLengthAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Int> = CoroutineScope(context).async { numberMaxLength { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimCardMaxCharacterLimits.nameMaxLength].
 */
fun SimCardMaxCharacterLimits.nameMaxLengthAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Int> = CoroutineScope(context).async { nameMaxLength { !isActive } }