package com.vestrel00.contacts.async.groups

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.groups.GroupsUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [GroupsUpdate.commit].
 */
suspend fun GroupsUpdate.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        GroupsUpdate.Result = withContext(context) { commit() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [GroupsUpdate.commit].
 */
fun GroupsUpdate.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<GroupsUpdate.Result> = CoroutineScope(context).async { commit() }