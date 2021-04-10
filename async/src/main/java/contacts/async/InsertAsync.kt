package contacts.async

import contacts.Insert
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.commit].
 */
suspend fun Insert.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER): Insert.Result =
    withContext(context) { commit { !isActive } }


/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.commit].
 */
fun Insert.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<Insert.Result> =
    CoroutineScope(context).async { commit { !isActive } }