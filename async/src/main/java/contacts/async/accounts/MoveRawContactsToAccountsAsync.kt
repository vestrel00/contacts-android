package contacts.async.accounts

import contacts.async.ASYNC_DISPATCHER
import contacts.core.accounts.MoveRawContactsAcrossAccounts
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MoveRawContactsAcrossAccounts.commit].
 */
suspend fun MoveRawContactsAcrossAccounts.commitWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): MoveRawContactsAcrossAccounts.Result = withContext(context) { commit { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [MoveRawContactsAcrossAccounts.commit].
 */
fun MoveRawContactsAcrossAccounts.commitAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<MoveRawContactsAcrossAccounts.Result> = CoroutineScope(context).async {
    commit { !isActive }
}