package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import contacts.core.entities.Contact
import contacts.core.entities.RawContactEntity
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.custom.GlobalCustomDataRegistry
import contacts.core.util.contact
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [RawContactEntity.contact].
 */
suspend fun RawContactEntity.contactWithContext(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) {
    contact(context, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [RawContactEntity.contact].
 */
fun RawContactEntity.contactAsync(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> = CoroutineScope(coroutineContext).async {
    contact(context, customDataRegistry) { !isActive }
}