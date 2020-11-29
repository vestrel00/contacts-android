package contacts.async

import contacts.Insert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [Insert.commit].
 */
suspend fun Insert.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER): Insert.Result =
    withContext(context) { commit() }


/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [Insert.commit].
 */
fun Insert.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<Insert.Result> =
    CoroutineScope(context).async { commit() }