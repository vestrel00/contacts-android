package contacts.async.groups

import contacts.async.ASYNC_DISPATCHER
import contacts.core.groups.GroupsDelete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsDelete.commit].
 */
suspend fun GroupsDelete.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        GroupsDelete.Result = withContext(context) { commit() }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [GroupsDelete.commitInOneTransaction].
 */
suspend fun GroupsDelete.commitInOneTransactionWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(context) { commitInOneTransaction() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsDelete.commit].
 */
fun GroupsDelete.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<GroupsDelete.Result> = CoroutineScope(context).async { commit() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [GroupsDelete.commitInOneTransaction].
 */
fun GroupsDelete.commitInOneTransactionAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<Boolean> =
    CoroutineScope(context).async { commitInOneTransaction() }