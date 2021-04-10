package contacts.async.groups

import contacts.async.ASYNC_DISPATCHER
import contacts.groups.GroupsUpdate
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsUpdate.commit].
 */
suspend fun GroupsUpdate.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        GroupsUpdate.Result = withContext(context) { commit { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsUpdate.commit].
 */
fun GroupsUpdate.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<GroupsUpdate.Result> = CoroutineScope(context).async { commit { !isActive } }