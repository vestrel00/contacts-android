package contacts.async.profile

import contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.profile.ProfileDelete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [ProfileDelete.commit].
 */
suspend fun ProfileDelete.commitWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): ProfileDelete.Result = withContext(context) { commit() }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [ProfileDelete.commitInOneTransaction].
 */
suspend fun ProfileDelete.commitInOneTransactionWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(context) { commitInOneTransaction() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [ProfileDelete.commit].
 */
fun ProfileDelete.commitAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ProfileDelete.Result> = CoroutineScope(context).async { commit() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [ProfileDelete.commitInOneTransaction].
 */
fun ProfileDelete.commitInOneTransactionAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<Boolean> = CoroutineScope(context).async { commitInOneTransaction() }