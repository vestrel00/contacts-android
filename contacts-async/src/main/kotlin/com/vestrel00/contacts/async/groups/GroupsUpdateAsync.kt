package com.vestrel00.contacts.async.groups

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.groups.GroupsUpdate
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the update operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * See [GroupsUpdate.commit].
 */
suspend fun GroupsUpdate.commitAsync(): GroupsUpdate.Result =
    withContext(ASYNC_DISPATCHER) { commit() }