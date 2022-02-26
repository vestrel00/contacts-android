package contacts.async.blockednumbers

import contacts.async.ASYNC_DISPATCHER
import contacts.core.blockednumbers.BlockedNumbersQuery
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlockedNumbersQuery.find].
 */
suspend fun BlockedNumbersQuery.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        BlockedNumbersQuery.Result = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlockedNumbersQuery.find].
 */
fun BlockedNumbersQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<BlockedNumbersQuery.Result> = CoroutineScope(context).async { find { !isActive } }