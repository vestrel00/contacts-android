package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import contacts.entities.BlankRawContact
import contacts.entities.RawContact
import contacts.util.toRawContact
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlankRawContact.toRawContact].
 */
suspend fun BlankRawContact.toRawContactWithContext(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): RawContact? = withContext(coroutineContext) { toRawContact(context) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [BlankRawContact.toRawContact].
 */
fun BlankRawContact.toRawContactAsync(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<RawContact?> =
    CoroutineScope(coroutineContext).async { toRawContact(context) { !isActive } }