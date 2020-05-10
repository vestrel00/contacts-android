package com.vestrel00.contacts.async.data

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.data.DataDelete
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the delete operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * See [DataDelete.commit].
 */
suspend fun DataDelete.commitAsync(): DataDelete.Result = withContext(ASYNC_DISPATCHER) { commit() }