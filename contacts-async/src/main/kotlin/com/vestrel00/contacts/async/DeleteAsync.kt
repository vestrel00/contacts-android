package com.vestrel00.contacts.async

import com.vestrel00.contacts.Delete
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the delete operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * See [Delete.commit].
 */
suspend fun Delete.commitAsync(): Delete.Result = withContext(ASYNC_DISPATCHER) { commit() }