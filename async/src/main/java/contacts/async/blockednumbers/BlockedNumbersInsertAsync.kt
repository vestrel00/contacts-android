package contacts.async.blockednumbers

import contacts.async.ASYNC_DISPATCHER
import contacts.core.blockednumbers.BlockedNumbersInsert
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlockedNumbersInsert.commit].
 */
suspend fun BlockedNumbersInsert.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        BlockedNumbersInsert.Result = withContext(context) { commit { !isActive } }


/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlockedNumbersInsert.commit].
 */
fun BlockedNumbersInsert.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<BlockedNumbersInsert.Result> =
    CoroutineScope(context).async { commit { !isActive } }