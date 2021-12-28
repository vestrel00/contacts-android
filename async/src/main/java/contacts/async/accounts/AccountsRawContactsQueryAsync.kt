package contacts.async.accounts

import contacts.async.ASYNC_DISPATCHER
import contacts.core.accounts.AccountsRawContactsQuery
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsRawContactsQuery.find].
 */
suspend fun AccountsRawContactsQuery.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        AccountsRawContactsQuery.Result = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsRawContactsQuery.find].
 */
fun AccountsRawContactsQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<AccountsRawContactsQuery.Result> =
    CoroutineScope(context).async { find { !isActive } }