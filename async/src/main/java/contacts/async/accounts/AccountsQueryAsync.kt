package contacts.async.accounts

import contacts.async.ASYNC_DISPATCHER
import contacts.core.accounts.AccountsQuery
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [AccountsQuery.find].
 */
suspend fun AccountsQuery.findWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): AccountsQuery.Result = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [AccountsQuery.find].
 */
fun AccountsQuery.findAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<AccountsQuery.Result> = CoroutineScope(context).async { find { !isActive } }