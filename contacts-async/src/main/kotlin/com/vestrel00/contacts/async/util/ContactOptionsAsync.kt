package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.ContactEntity
import com.vestrel00.contacts.entities.MutableOptions
import com.vestrel00.contacts.util.setOptions
import com.vestrel00.contacts.util.updateOptions
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.setOptions].
 */
suspend fun ContactEntity.setOptionsAsync(context: Context, options: MutableOptions): Boolean =
    withContext(ASYNC_DISPATCHER) { setOptions(context, options) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.updateOptions].
 */
suspend fun ContactEntity.updateOptionsAsync(
    context: Context, update: MutableOptions.() -> Unit
): Boolean = withContext(ASYNC_DISPATCHER) { updateOptions(context, update) }