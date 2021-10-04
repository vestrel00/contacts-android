package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import contacts.core.entities.Contact
import contacts.core.entities.MutableContact
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
 * See [Contact.refresh].
 */
suspend fun Contact.refreshWithContext(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) { refresh(context, customDataRegistry) { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Contact.refresh].
 */
suspend fun MutableContact.refreshWithContext(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): MutableContact? = withContext(coroutineContext) {
    refresh(context, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Contact.refresh].
 */
fun Contact.refreshAsync(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> = CoroutineScope(coroutineContext).async {
    refresh(context, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Contact.refresh].
 */
fun MutableContact.refreshAsync(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<MutableContact?> =
    CoroutineScope(coroutineContext).async { refresh(context, customDataRegistry) { !isActive } }