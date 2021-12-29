package contacts.async.data

import contacts.async.ASYNC_DISPATCHER
import contacts.core.AbstractDataFieldSet
import contacts.core.DataField
import contacts.core.data.DataQuery
import contacts.core.entities.ExistingDataEntity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.find].
 */
suspend fun <F : DataField, S : AbstractDataFieldSet<F>, E : ExistingDataEntity> DataQuery<F, S, E>.findWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): DataQuery.Result<E> = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.find].
 */
fun <F : DataField, S : AbstractDataFieldSet<F>, E : ExistingDataEntity> DataQuery<F, S, E>.findAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<DataQuery.Result<E>> = CoroutineScope(context).async { find { !isActive } }