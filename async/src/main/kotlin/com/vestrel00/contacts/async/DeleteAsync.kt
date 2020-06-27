package com.vestrel00.contacts.async

import com.vestrel00.contacts.Delete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [Delete.commit].
 */
suspend fun Delete.commitWithContext(context: CoroutineContext = ASYNC_DISPATCHER): Delete.Result =
    withContext(context) { commit() }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * See [Delete.commitInOneTransaction].
 */
suspend fun Delete.commitInOneTransactionWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        Boolean = withContext(context) { commitInOneTransaction() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [Delete.commit].
 */
fun Delete.commitAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<Delete.Result> =
    CoroutineScope(context).async { commit() }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * See [Delete.commitInOneTransaction].
 */
fun Delete.commitInOneTransactionAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<Boolean> = CoroutineScope(context).async { commitInOneTransaction() }