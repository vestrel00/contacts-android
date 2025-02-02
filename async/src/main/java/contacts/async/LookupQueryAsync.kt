package contacts.async

import contacts.core.LookupQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [LookupQuery.find].
 */
suspend fun LookupQuery.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        LookupQuery.Result = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [LookupQuery.find].
 */
fun LookupQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<LookupQuery.Result> =
    CoroutineScope(context).async { find { !isActive } }