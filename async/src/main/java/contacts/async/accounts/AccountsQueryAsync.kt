package contacts.async.accounts

import android.accounts.Account
import contacts.async.ASYNC_DISPATCHER
import contacts.core.accounts.AccountsQuery
import contacts.core.entities.RawContactEntity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT
/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [AccountsQuery.accountFor].
 */
suspend fun AccountsQuery.accountForWithContext(
    rawContact: RawContactEntity,
    context: CoroutineContext = ASYNC_DISPATCHER
): Account? = withContext(context) { accountFor(rawContact) }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *´
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsQuery.accountsFor].
 */
suspend fun AccountsQuery.accountsForWithContext(
    vararg rawContacts: RawContactEntity,
    context: CoroutineContext = ASYNC_DISPATCHER
) = accountsForWithContext(rawContacts.asSequence(), context)

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsQuery.accountsFor].
 */
suspend fun AccountsQuery.accountsForWithContext(
    rawContacts: Collection<RawContactEntity>,
    context: CoroutineContext = ASYNC_DISPATCHER
) = accountsForWithContext(rawContacts.asSequence(), context)

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsQuery.accountsFor].
 */
suspend fun AccountsQuery.accountsForWithContext(
    rawContacts: Sequence<RawContactEntity>,
    context: CoroutineContext = ASYNC_DISPATCHER
): AccountsQuery.AccountsList = withContext(context) { accountsFor(rawContacts) { !isActive } }

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [AccountsQuery.accountFor].
 */
fun AccountsQuery.accountForAsync(
    rawContact: RawContactEntity,
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Account?> = CoroutineScope(context).async { accountFor(rawContact) }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsQuery.accountsFor].
 */
fun AccountsQuery.accountsForAsync(
    vararg rawContacts: RawContactEntity,
    context: CoroutineContext = ASYNC_DISPATCHER
) = accountsForAsync(rawContacts.asSequence(), context)

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsQuery.accountsFor].
 */
fun AccountsQuery.accountsForAsync(
    rawContacts: Collection<RawContactEntity>,
    context: CoroutineContext = ASYNC_DISPATCHER
) = accountsForAsync(rawContacts.asSequence(), context)

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsQuery.accountsFor].
 */
fun AccountsQuery.accountsForAsync(
    rawContacts: Sequence<RawContactEntity>,
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<AccountsQuery.AccountsList> = CoroutineScope(context).async { accountsFor(rawContacts) }

// endregion