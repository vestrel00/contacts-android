package contacts.async

import contacts.core.RawContactsQuery
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [RawContactsQuery.find].
 */
suspend fun RawContactsQuery.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        RawContactsQuery.Result = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [RawContactsQuery.find].
 */
fun RawContactsQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<RawContactsQuery.Result> =
    CoroutineScope(context).async { find { !isActive } }