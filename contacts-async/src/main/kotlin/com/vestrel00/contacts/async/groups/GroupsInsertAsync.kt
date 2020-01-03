package com.vestrel00.contacts.async.groups

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.groups.GroupsInsert
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the insert operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * See [GroupsInsert.commit].
 */
suspend fun GroupsInsert.commitAsync(): GroupsInsert.Result =
    withContext(ASYNC_DISPATCHER) { commit() }