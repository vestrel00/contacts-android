package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.BlankRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.util.toRawContact
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [BlankRawContact.toRawContact].
 */
suspend fun BlankRawContact.toRawContactAsync(context: Context): RawContact? =
    withContext(ASYNC_DISPATCHER) { toRawContact(context) { !isActive } }