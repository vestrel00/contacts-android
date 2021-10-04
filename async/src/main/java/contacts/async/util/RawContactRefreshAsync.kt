package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.custom.GlobalCustomDataRegistry
import contacts.core.util.refresh
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [RawContact.refresh].
 */
suspend fun RawContact.refreshWithContext(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): RawContact? = withContext(coroutineContext) {
    refresh(context, customDataRegistry) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [RawContact.refresh].
 */
suspend fun MutableRawContact.refreshWithContext(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): MutableRawContact? = withContext(coroutineContext) {
    refresh(context, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [RawContact.refresh].
 */
fun RawContact.refreshAsync(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<RawContact?> = CoroutineScope(coroutineContext).async {
    refresh(context, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [RawContact.refresh].
 */
fun MutableRawContact.refreshAsync(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<MutableRawContact?> = CoroutineScope(coroutineContext).async {
    refresh(context, customDataRegistry) { !isActive }
}