package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.Group
import contacts.core.entities.MutableGroup
import contacts.core.groups.GroupsInsert
import contacts.core.util.group
import contacts.core.util.groups
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsInsert.Result.group].
 */
suspend fun GroupsInsert.Result.groupWithContext(
    contacts: Contacts,
    group: MutableGroup,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Group? = withContext(coroutineContext) { group(contacts, group) { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsInsert.Result.groups].
 */
suspend fun GroupsInsert.Result.groupsWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<Group> = withContext(coroutineContext) { groups(contacts) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsInsert.Result.group].
 */
fun GroupsInsert.Result.groupAsync(
    contacts: Contacts,
    group: MutableGroup,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Group?> =
    CoroutineScope(coroutineContext).async { group(contacts, group) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsInsert.Result.groups].
 */
fun GroupsInsert.Result.groupsAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<Group>> = CoroutineScope(coroutineContext).async { groups(contacts) { !isActive } }