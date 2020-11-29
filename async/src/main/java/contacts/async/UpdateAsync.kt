package contacts.async

import contacts.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [Update.commit].
 */
suspend fun Update.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER): Update.Result =
    withContext(context) { commit() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [Update.commit].
 */
fun Update.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<Update.Result> =
    CoroutineScope(context).async { commit() }