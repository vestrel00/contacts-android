package com.vestrel00.contacts.async

import com.vestrel00.contacts.Update
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the update operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * See [Update.commit].
 */
suspend fun Update.commitAsync(): Update.Result = withContext(ASYNC_DISPATCHER) { commit() }