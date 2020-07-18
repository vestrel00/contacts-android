package com.vestrel00.contacts.async

import com.vestrel00.contacts.GeneralQuery
import com.vestrel00.contacts.entities.Contact
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GeneralQuery.find].
 */
suspend fun GeneralQuery.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        List<Contact> = withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [GeneralQuery.find].
 */
fun GeneralQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<List<Contact>> =
    CoroutineScope(context).async { find { !isActive } }