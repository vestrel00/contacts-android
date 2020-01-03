package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.MutableOptions
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.util.setOptions
import com.vestrel00.contacts.util.updateOptions
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [RawContact.setOptions].
 */
suspend fun RawContact.setOptionsAsync(context: Context, options: MutableOptions): Boolean =
    withContext(ASYNC_DISPATCHER) { setOptions(context, options) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [RawContact.updateOptions].
 */
suspend fun RawContact.updateOptionsAsync(
    context: Context, update: MutableOptions.() -> Unit
): Boolean = withContext(ASYNC_DISPATCHER) { updateOptions(context, update) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [MutableRawContact.setOptions].
 */
suspend fun MutableRawContact.setOptionsAsync(context: Context, options: MutableOptions): Boolean =
    withContext(ASYNC_DISPATCHER) { setOptions(context, options) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [MutableRawContact.updateOptions].
 */
suspend fun MutableRawContact.updateOptionsAsync(
    context: Context, update: MutableOptions.() -> Unit
): Boolean = withContext(ASYNC_DISPATCHER) { updateOptions(context, update) }