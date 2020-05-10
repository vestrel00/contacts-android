package com.vestrel00.contacts.async.data

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.data.DataUpdate
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the update operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * See [DataUpdate.commit].
 */
suspend fun DataUpdate.commitAsync(): DataUpdate.Result = withContext(ASYNC_DISPATCHER) { commit() }