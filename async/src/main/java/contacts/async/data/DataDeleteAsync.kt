package contacts.async.data

import contacts.async.ASYNC_DISPATCHER
import contacts.data.DataDelete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [DataDelete.commit].
 */
suspend fun DataDelete.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER): DataDelete.Result =
    withContext(context) { commit() }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [DataDelete.commitInOneTransaction].
 */
suspend fun DataDelete.commitInOneTransactionWithContext(context: CoroutineContext = ASYNC_DISPATCHER): Boolean =
    withContext(context) { commitInOneTransaction() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [DataDelete.commit].
 */
fun DataDelete.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<DataDelete.Result> =
    CoroutineScope(context).async { commit() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [DataDelete.commitInOneTransaction].
 */
fun DataDelete.commitInOneTransactionAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<Boolean> =
    CoroutineScope(context).async { commitInOneTransaction() }