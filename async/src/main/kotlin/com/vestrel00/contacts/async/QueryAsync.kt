package com.vestrel00.contacts.async

import com.vestrel00.contacts.Query
import com.vestrel00.contacts.entities.Contact
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Query.find].
 */
suspend fun Query.findWithContext(context: CoroutineContext = ASYNC_DISPATCHER): List<Contact> =
    withContext(context) { find { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Query.findFirst].
 */
suspend fun Query.findFirstWithContext(context: CoroutineContext = ASYNC_DISPATCHER): Contact? =
    findWithContext(context).firstOrNull()

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Query.find].
 */
fun Query.findAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<List<Contact>> =
    CoroutineScope(context).async { find { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [context], performs the operation in that scope, then
 * returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Query.findFirst].
 */
fun Query.findFirstAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<Contact?> =
    CoroutineScope(context).async { findFirst { !isActive } }