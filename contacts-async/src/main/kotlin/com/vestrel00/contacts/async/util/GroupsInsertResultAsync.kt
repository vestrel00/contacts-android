package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.MutableGroup
import com.vestrel00.contacts.groups.GroupsInsert
import com.vestrel00.contacts.util.group
import com.vestrel00.contacts.util.groups
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsInsert.Result.group].
 */
suspend fun GroupsInsert.Result.groupAsync(context: Context, group: MutableGroup): Group? =
    withContext(ASYNC_DISPATCHER) { group(context, group) { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [GroupsInsert.Result.groups].
 */
suspend fun GroupsInsert.Result.groupsAsync(context: Context): List<Group> =
    withContext(ASYNC_DISPATCHER) { groups(context) { !isActive } }