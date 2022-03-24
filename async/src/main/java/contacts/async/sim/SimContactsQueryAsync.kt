package contacts.async.sim

import contacts.async.ASYNC_DISPATCHER
import contacts.core.sim.SimContactsQuery
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsQuery.find].
 */
suspend fun SimContactsQuery.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        SimContactsQuery.Result = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsQuery.find].
 */
fun SimContactsQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<SimContactsQuery.Result> = CoroutineScope(context).async { find { !isActive } }