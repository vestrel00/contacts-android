package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.ImmutableDataEntity
import contacts.core.entities.MutableDataEntity
import contacts.core.util.refresh
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ImmutableDataEntity.refresh].
 */
suspend fun <T : ImmutableDataEntity> T.refreshWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): T? = withContext(coroutineContext) {
    refresh(contacts) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ImmutableDataEntity.refresh].
 */
suspend fun <T : MutableDataEntity> T.refreshWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): T? = withContext(coroutineContext) {
    refresh(contacts) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MutableDataEntity.refresh].
 */
fun <T : ImmutableDataEntity> T.refreshAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<T?> = CoroutineScope(coroutineContext).async {
    refresh(contacts) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MutableDataEntity.refresh].
 */
fun <T : MutableDataEntity> T.refreshAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<T?> = CoroutineScope(coroutineContext).async {
    refresh(contacts) { !isActive }
}