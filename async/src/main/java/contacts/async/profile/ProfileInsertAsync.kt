package contacts.async.profile

import contacts.async.ASYNC_DISPATCHER
import contacts.profile.ProfileInsert
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
 * See [ProfileInsert.commit].
 */
suspend fun ProfileInsert.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        ProfileInsert.Result = withContext(context) { commit() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.commit].
 */
fun ProfileInsert.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<ProfileInsert.Result> = CoroutineScope(context).async { commit() }