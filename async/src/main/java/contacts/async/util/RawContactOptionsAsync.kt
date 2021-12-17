package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.MutableOptionsEntity
import contacts.core.entities.Options
import contacts.core.util.options
import contacts.core.util.setOptions
import contacts.core.util.updateOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingRawContactEntity.options].
 */
suspend fun ExistingRawContactEntity.optionsWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Options? = withContext(coroutineContext) { options(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingRawContactEntity.setOptions].
 */
suspend fun ExistingRawContactEntity.setOptionsWithContext(
    contacts: Contacts,
    options: MutableOptionsEntity,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setOptions(contacts, options) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingRawContactEntity.updateOptions].
 */
suspend fun ExistingRawContactEntity.updateOptionsWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER,
    update: MutableOptionsEntity.() -> Unit
): Boolean = withContext(coroutineContext) { updateOptions(contacts, update) }

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntity.options].
 */
fun ExistingRawContactEntity.optionsAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Options?> = CoroutineScope(coroutineContext).async { options(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntity.setOptions].
 */
fun ExistingRawContactEntity.setOptionsAsync(
    contacts: Contacts,
    options: MutableOptionsEntity,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setOptions(contacts, options) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntity.updateOptions].
 */
fun ExistingRawContactEntity.updateOptionsAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER,
    update: MutableOptionsEntity.() -> Unit
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { updateOptions(contacts, update) }

// endregion