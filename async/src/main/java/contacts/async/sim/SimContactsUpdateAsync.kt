package contacts.async.sim

import contacts.async.ASYNC_DISPATCHER
import contacts.core.sim.SimContactsUpdate
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsUpdate.commit].
 */
suspend fun SimContactsUpdate.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        SimContactsUpdate.Result = withContext(context) { commit { !isActive } }


/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsUpdate.commit].
 */
fun SimContactsUpdate.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<SimContactsUpdate.Result> = CoroutineScope(context).async { commit { !isActive } }