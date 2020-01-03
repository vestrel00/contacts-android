package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.util.refresh
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the refresh operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [Contact.refresh].
 */
suspend fun Contact.refreshAsync(context: Context): Contact? =
    withContext(ASYNC_DISPATCHER) { refresh(context) { !isActive } }

/**
 * Suspends the current coroutine, performs the refresh operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [Contact.refresh].
 */
suspend fun MutableContact.refreshAsync(context: Context): MutableContact? =
    withContext(ASYNC_DISPATCHER) { refresh(context) { !isActive } }