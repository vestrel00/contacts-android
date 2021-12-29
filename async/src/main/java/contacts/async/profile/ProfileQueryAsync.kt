package contacts.async.profile

import contacts.async.ASYNC_DISPATCHER
import contacts.core.profile.ProfileQuery
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileQuery.find].
 */
suspend fun ProfileQuery.findWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): ProfileQuery.Result = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileQuery.find].
 */
fun ProfileQuery.findAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ProfileQuery.Result> = CoroutineScope(context).async { find { !isActive } }