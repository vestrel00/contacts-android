package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.util.refresh
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the refresh operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [RawContact.refresh].
 */
suspend fun RawContact.refreshAsync(context: Context): RawContact? =
    withContext(ASYNC_DISPATCHER) { refresh(context) { !isActive } }

/**
 * Suspends the current coroutine, performs the refresh operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [RawContact.refresh].
 */
suspend fun MutableRawContact.refreshAsync(context: Context): MutableRawContact? =
    withContext(ASYNC_DISPATCHER) { refresh(context) { !isActive } }