package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import contacts.entities.CommonDataEntity
import contacts.entities.RawContact
import contacts.entities.custom.CustomCommonDataRegistry
import contacts.entities.custom.GlobalCustomCommonDataRegistry
import contacts.util.rawContact
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [CommonDataEntity.rawContact].
 */
suspend fun CommonDataEntity.rawContactWithContext(
    context: Context,
    customDataRegistry: CustomCommonDataRegistry = GlobalCustomCommonDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): RawContact? = withContext(coroutineContext) {
    rawContact(context, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [CommonDataEntity.rawContact].
 */
fun CommonDataEntity.rawContactAsync(
    context: Context,
    customDataRegistry: CustomCommonDataRegistry = GlobalCustomCommonDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<RawContact?> = CoroutineScope(coroutineContext).async {
    rawContact(context, customDataRegistry) { !isActive }
}