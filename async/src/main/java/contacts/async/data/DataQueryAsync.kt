package contacts.async.data

import contacts.async.ASYNC_DISPATCHER
import contacts.core.CommonDataField
import contacts.core.data.CommonDataQuery
import contacts.core.entities.CommonDataEntity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [CommonDataQuery.find].
 */
suspend fun <K : CommonDataField, V : CommonDataEntity> CommonDataQuery<K, V>.findWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): List<V> = withContext(context) { find { !isActive } }


/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [CommonDataQuery.find].
 */
fun <K : CommonDataField, V : CommonDataEntity> CommonDataQuery<K, V>.findAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<V>> = CoroutineScope(context).async { find { !isActive } }