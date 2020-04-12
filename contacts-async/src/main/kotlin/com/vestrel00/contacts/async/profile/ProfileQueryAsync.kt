package com.vestrel00.contacts.async.profile

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.profile.ProfileQuery
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileQuery.find].
 */
suspend fun ProfileQuery.findAsync(): Contact? =
    withContext(ASYNC_DISPATCHER) { find { !isActive } }