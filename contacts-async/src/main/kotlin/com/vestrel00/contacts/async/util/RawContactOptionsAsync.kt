package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.MutableOptions
import com.vestrel00.contacts.entities.Options
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.util.options
import com.vestrel00.contacts.util.setOptions
import com.vestrel00.contacts.util.updateOptions
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [RawContactEntity.options].
 */
suspend fun RawContactEntity.optionsAsync(context: Context): Options =
    withContext(ASYNC_DISPATCHER) { options(context) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [RawContactEntity.setOptions].
 */
suspend fun RawContactEntity.setOptionsAsync(context: Context, options: MutableOptions): Boolean =
    withContext(ASYNC_DISPATCHER) { setOptions(context, options) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [RawContactEntity.updateOptions].
 */
suspend fun RawContactEntity.updateOptionsAsync(
    context: Context, update: MutableOptions.() -> Unit
): Boolean = withContext(ASYNC_DISPATCHER) { updateOptions(context, update) }