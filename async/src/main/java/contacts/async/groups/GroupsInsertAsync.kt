package contacts.async.groups

import contacts.async.ASYNC_DISPATCHER
import contacts.core.groups.GroupsInsert
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsInsert.commit].
 */
suspend fun GroupsInsert.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        GroupsInsert.Result = withContext(context) { commit { !isActive } }


/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsInsert.commit].
 */
fun GroupsInsert.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<GroupsInsert.Result> = CoroutineScope(context).async { commit { !isActive } }