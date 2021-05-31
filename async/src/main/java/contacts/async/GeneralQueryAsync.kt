package contacts.async

import contacts.BroadQuery
import contacts.entities.Contact
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
        List<Contact> = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BroadQuery.find].
 */
fun BroadQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<List<Contact>> =
    CoroutineScope(context).async { find { !isActive } }