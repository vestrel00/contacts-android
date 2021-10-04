package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import contacts.core.entities.Contact
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.custom.GlobalCustomDataRegistry
import contacts.core.util.ContactLinkResult
import contacts.core.util.ContactUnlinkResult
import contacts.core.util.contact
import contacts.core.util.contacts
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ContactLinkResult.contact].
 */
suspend fun ContactLinkResult.contactWithContext(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) {
    contact(context, customDataRegistry) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ContactUnlinkResult.contacts].
 */
suspend fun ContactUnlinkResult.contactsWithContext(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<Contact> = withContext(coroutineContext) {
    contacts(context, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ContactLinkResult.contact].
 */
fun ContactLinkResult.contactAsync(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> = CoroutineScope(coroutineContext).async {
    contact(context, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ContactUnlinkResult.contacts].
 */
fun ContactUnlinkResult.contactsAsync(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<Contact>> = CoroutineScope(coroutineContext).async {
    contacts(context, customDataRegistry) { !isActive }
}