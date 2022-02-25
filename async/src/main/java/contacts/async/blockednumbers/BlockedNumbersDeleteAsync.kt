package contacts.async.blockednumbers

import contacts.async.ASYNC_DISPATCHER
import contacts.core.blockednumbers.BlockedNumbersDelete
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
 * See [BlockedNumbersDelete.commit].
 */
suspend fun BlockedNumbersDelete.commitWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): BlockedNumbersDelete.Result = withContext(context) { commit() }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [BlockedNumbersDelete.commitInOneTransaction].
 */
suspend fun BlockedNumbersDelete.commitInOneTransactionWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): BlockedNumbersDelete.Result = withContext(context) { commitInOneTransaction() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlockedNumbersDelete.commit].
 */
fun BlockedNumbersDelete.commitAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<BlockedNumbersDelete.Result> = CoroutineScope(context).async { commit() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [BlockedNumbersDelete.commitInOneTransaction].
 */
fun BlockedNumbersDelete.commitInOneTransactionAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<BlockedNumbersDelete.Result> =
    CoroutineScope(context).async { commitInOneTransaction() }