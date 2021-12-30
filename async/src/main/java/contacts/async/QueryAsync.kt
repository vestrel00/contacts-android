package contacts.async

import contacts.core.Query
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Query.find].
 */
suspend fun Query.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER): Query.Result =
    withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Query.find].
 */
fun Query.findAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<Query.Result> =
    CoroutineScope(context).async { find { !isActive } }