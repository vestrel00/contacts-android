package com.vestrel00.contacts.async.groups

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.groups.GroupsQuery
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsQuery.find].
 */
suspend fun GroupsQuery.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        GroupsQuery.GroupsList = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsQuery.find].
 */
fun GroupsQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<GroupsQuery.GroupsList> = CoroutineScope(context).async { find { !isActive } }