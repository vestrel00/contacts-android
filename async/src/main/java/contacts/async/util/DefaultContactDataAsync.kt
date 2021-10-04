package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import contacts.core.entities.CommonDataEntity
import contacts.core.util.clearDefault
import contacts.core.util.setAsDefault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [CommonDataEntity.setAsDefault].
 */
suspend fun CommonDataEntity.setAsDefaultWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setAsDefault(context) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [CommonDataEntity.clearDefault].
 */
suspend fun CommonDataEntity.clearDefaultWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { clearDefault(context) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [CommonDataEntity.setAsDefault].
 */
fun CommonDataEntity.setAsDefaultAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setAsDefault(context) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [CommonDataEntity.clearDefault].
 */
fun CommonDataEntity.clearDefaultAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { clearDefault(context) }