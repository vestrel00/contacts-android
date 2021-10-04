package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import contacts.core.entities.Group
import contacts.core.entities.GroupMembership
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
 *
 * See [group].
 */
suspend fun GroupMembership.groupWithContext(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Group? = withContext(coroutineContext) { group(context) { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [groups].
 */
suspend fun Collection<GroupMembership>.groupsWithContext(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<Group> = withContext(coroutineContext) { groups(context) { !isActive } }

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [group].
 */
fun GroupMembership.groupAsync(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Group?> = CoroutineScope(coroutineContext).async { group(context) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [groups].
 */
fun Collection<GroupMembership>.groupsAsync(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<Group>> = CoroutineScope(coroutineContext).async { groups(context) { !isActive } }

// endregion