package contacts.async

import contacts.core.Insert
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
 * See [Insert.commit].
 */
suspend fun Insert.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER): Insert.Result =
    withContext(context) { commit { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.commitInChunks].
 */
suspend fun Insert.commitInChunksWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): Insert.Result = withContext(context) { commitInChunks { !isActive } }

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

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.commitInChunks].
 */
fun Insert.commitInChunksAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Insert.Result> = CoroutineScope(context).async { commitInChunks { !isActive } }