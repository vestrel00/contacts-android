package contacts.async.groups

import contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.groups.GroupsInsert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [GroupsInsert.commit].
 */
suspend fun GroupsInsert.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        GroupsInsert.Result = withContext(context) { commit() }


/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [GroupsInsert.commit].
 */
fun GroupsInsert.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<GroupsInsert.Result> = CoroutineScope(context).async { commit() }