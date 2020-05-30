package com.vestrel00.contacts.async.profile

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.profile.ProfileQuery
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileQuery.find].
 */
suspend fun ProfileQuery.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER): Contact? =
    withContext(context) { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileQuery.find].
 */
fun ProfileQuery.findAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<Contact?> =
    CoroutineScope(context).async { find { !isActive } }