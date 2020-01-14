package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.DataEntity
import com.vestrel00.contacts.util.clearDefault
import com.vestrel00.contacts.util.setAsDefault
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [DataEntity.setAsDefault].
 */
suspend fun DataEntity.setAsDefaultAsync(context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { setAsDefault(context) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [DataEntity.clearDefault].
 */
suspend fun DataEntity.clearDefaultAsync(context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { clearDefault(context) }