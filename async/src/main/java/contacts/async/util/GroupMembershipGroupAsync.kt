package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.Group
import contacts.core.entities.GroupMembershipEntity
import contacts.core.util.group
import contacts.core.util.groups
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 */
suspend fun GroupMembershipEntity.groupWithContext(
    contacts: Contacts, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Group? = withContext(coroutineContext) { group(contacts) { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 */
suspend fun Collection<GroupMembershipEntity>.groupsWithContext(
    contacts: Contacts, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<Group> = withContext(coroutineContext) { groups(contacts) { !isActive } }

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 */
fun GroupMembershipEntity.groupAsync(
    contacts: Contacts, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Group?> = CoroutineScope(coroutineContext).async { group(contacts) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 */
fun Collection<GroupMembershipEntity>.groupsAsync(
    contacts: Contacts, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<Group>> = CoroutineScope(coroutineContext).async { groups(contacts) { !isActive } }

// endregion