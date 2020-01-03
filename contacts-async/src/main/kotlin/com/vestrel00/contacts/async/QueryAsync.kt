package com.vestrel00.contacts.async

import com.vestrel00.contacts.Query
import com.vestrel00.contacts.entities.Contact
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [Query.find].
 */
suspend fun Query.findAsync(): List<Contact> = withContext(ASYNC_DISPATCHER) { find { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [Query.findFirst].
 */
suspend fun Query.findFirstAsync(): Contact? = findAsync().firstOrNull()