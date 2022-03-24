package contacts.async.sim

import contacts.async.ASYNC_DISPATCHER
import contacts.core.sim.SimContactsInsert
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsInsert.commit].
 */
suspend fun SimContactsInsert.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        SimContactsInsert.Result = withContext(context) { commit { !isActive } }


/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsInsert.commit].
 */
fun SimContactsInsert.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<SimContactsInsert.Result> = CoroutineScope(context).async { commit { !isActive } }