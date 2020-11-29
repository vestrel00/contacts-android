package contacts.async.data

import contacts.CommonDataField
import contacts.async.ASYNC_DISPATCHER
import contacts.data.CommonDataQuery
import contacts.entities.CommonDataEntity
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
suspend fun <T : CommonDataField, R : CommonDataEntity> CommonDataQuery<T, R>.findWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): List<R> = withContext(context) { find { !isActive } }


/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [CommonDataQuery.find].
 */
fun <T : CommonDataField, R : CommonDataEntity> CommonDataQuery<T, R>.findAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<R>> = CoroutineScope(context).async { find { !isActive } }