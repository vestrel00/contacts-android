package contacts.async

import contacts.core.PhoneLookupQuery
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [PhoneLookupQuery.find].
 */
suspend fun PhoneLookupQuery.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        PhoneLookupQuery.Result = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [PhoneLookupQuery.find].
 */
fun PhoneLookupQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<PhoneLookupQuery.Result> =
    CoroutineScope(context).async { find { !isActive } }