package com.vestrel00.contacts.async.groups

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.groups.GroupsQuery
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsQuery.find].
 */
suspend fun GroupsQuery.findAsync(): List<Group> =
    withContext(ASYNC_DISPATCHER) { find { !isActive } }


/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsQuery.findFirst].
 */
suspend fun GroupsQuery.findFirstAsync(): Group? = findAsync().firstOrNull()