package contacts.async.profile

import contacts.async.ASYNC_DISPATCHER
import contacts.core.profile.ProfileUpdate
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileUpdate.commit].
 */
suspend fun ProfileUpdate.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        ProfileUpdate.Result = withContext(context) { commit { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileUpdate.commit].
 */
fun ProfileUpdate.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<ProfileUpdate.Result> = CoroutineScope(context).async { commit { !isActive } }