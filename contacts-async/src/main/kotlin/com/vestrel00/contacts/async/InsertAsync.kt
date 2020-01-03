package com.vestrel00.contacts.async

import com.vestrel00.contacts.Insert
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the insert operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * See [Insert.commit].
 */
suspend fun Insert.commitAsync(): Insert.Result = withContext(ASYNC_DISPATCHER) { commit() }