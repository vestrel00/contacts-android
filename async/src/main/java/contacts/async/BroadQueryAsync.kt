package contacts.async

import contacts.core.BroadQuery
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BroadQuery.find].
 */
suspend fun BroadQuery.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        BroadQuery.Result = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BroadQuery.find].
 */
fun BroadQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<BroadQuery.Result> =
    CoroutineScope(context).async { find { !isActive } }