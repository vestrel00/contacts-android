package contacts.async.sim

import contacts.async.ASYNC_DISPATCHER
import contacts.core.sim.SimContactsDelete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsDelete.commit].
 */
suspend fun SimContactsDelete.commitWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): SimContactsDelete.Result = withContext(context) { commit() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [SimContactsDelete.commit].
 */
fun SimContactsDelete.commitAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<SimContactsDelete.Result> = CoroutineScope(context).async { commit() }