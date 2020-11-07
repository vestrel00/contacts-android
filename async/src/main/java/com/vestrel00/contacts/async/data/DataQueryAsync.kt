package com.vestrel00.contacts.async.data

import com.vestrel00.contacts.CommonDataField
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.data.CommonDataQuery
import com.vestrel00.contacts.entities.CommonDataEntity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [CommonDataQuery.find].
 */
suspend fun <T : CommonDataField, R : CommonDataEntity> CommonDataQuery<T, R>.findWithContext(
    context: CoroutineContext = ASYNC_DISPATCHER
): List<R> = withContext(context) { find { !isActive } }


/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [CommonDataQuery.find].
 */
fun <T : CommonDataField, R : CommonDataEntity> CommonDataQuery<T, R>.findAsync(
    context: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<R>> = CoroutineScope(context).async { find { !isActive } }