package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.MutableOptions
import com.vestrel00.contacts.entities.Options
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.util.options
import com.vestrel00.contacts.util.setOptions
import com.vestrel00.contacts.util.updateOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [RawContactEntity.options].
 */
suspend fun RawContactEntity.optionsWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Options = withContext(coroutineContext) { options(context) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [RawContactEntity.setOptions].
 */
suspend fun RawContactEntity.setOptionsWithContext(
    context: Context,
    options: MutableOptions,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setOptions(context, options) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [RawContactEntity.updateOptions].
 */
suspend fun RawContactEntity.updateOptionsWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER,
    update: MutableOptions.() -> Unit
): Boolean = withContext(coroutineContext) { updateOptions(context, update) }

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.options].
 */
fun RawContactEntity.optionsAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Options> = CoroutineScope(coroutineContext).async { options(context) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.setOptions].
 */
fun RawContactEntity.setOptionsAsync(
    context: Context,
    options: MutableOptions,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setOptions(context, options) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.updateOptions].
 */
fun RawContactEntity.updateOptionsAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER,
    update: MutableOptions.() -> Unit
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { updateOptions(context, update) }

// endregion