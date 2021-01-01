package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import contacts.entities.Contact
import contacts.entities.RawContact
import contacts.entities.custom.CustomCommonDataRegistry
import contacts.entities.custom.GlobalCustomCommonDataRegistry
import contacts.profile.ProfileInsert
import contacts.util.contact
import contacts.util.rawContact
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.rawContact].
 */
suspend fun ProfileInsert.Result.rawContactWithContext(
    context: Context,
    customDataRegistry: CustomCommonDataRegistry = GlobalCustomCommonDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): RawContact? = withContext(coroutineContext) {
    rawContact(context, customDataRegistry) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.contact].
 */
suspend fun ProfileInsert.Result.contactWithContext(
    context: Context,
    customDataRegistry: CustomCommonDataRegistry = GlobalCustomCommonDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) {
    contact(context, customDataRegistry) { !isActive }
}

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.rawContact].
 */
fun ProfileInsert.Result.rawContactAsync(
    context: Context,
    customDataRegistry: CustomCommonDataRegistry = GlobalCustomCommonDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<RawContact?> = CoroutineScope(coroutineContext).async {
    rawContact(context, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.contact].
 */
fun ProfileInsert.Result.contactAsync(
    context: Context,
    customDataRegistry: CustomCommonDataRegistry = GlobalCustomCommonDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> = CoroutineScope(coroutineContext).async {
    contact(context, customDataRegistry) { !isActive }
}

// endregion