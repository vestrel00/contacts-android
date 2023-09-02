package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.ExistingDataEntity
import contacts.core.util.isReadOnly
import contacts.core.util.isReadOnlyMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ExistingDataEntity.isReadOnly].
 */
suspend fun ExistingDataEntity.isReadOnlyWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) {
    isReadOnly(contacts) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [isReadOnlyMap].
 */
suspend fun Collection<ExistingDataEntity>.isReadOnlyMapWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Map<Long, Boolean> = withContext(coroutineContext) {
    isReadOnlyMap(contacts) { !isActive }
}

// endregion

// region WITH ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ExistingDataEntity.isReadOnly].
 */
fun ExistingDataEntity.isReadOnlyAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async {
    isReadOnly(contacts) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [isReadOnlyMap].
 */
fun Collection<ExistingDataEntity>.isReadOnlyMapAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Map<Long, Boolean>> = CoroutineScope(coroutineContext).async {
    isReadOnlyMap(contacts) { !isActive }
}

// endregion