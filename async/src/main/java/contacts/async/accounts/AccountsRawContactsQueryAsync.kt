package contacts.async.accounts

import com.vestrel00.contacts.accounts.AccountsRawContactsQuery
import contacts.async.ASYNC_DISPATCHER
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
        AccountsRawContactsQuery.BlankRawContactsList = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsRawContactsQuery.find].
 */
fun AccountsRawContactsQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<AccountsRawContactsQuery.BlankRawContactsList> =
    CoroutineScope(context).async { find { !isActive } }