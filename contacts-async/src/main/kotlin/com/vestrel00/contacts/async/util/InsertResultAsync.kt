package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.Insert
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.util.contact
import com.vestrel00.contacts.util.contacts
import com.vestrel00.contacts.util.rawContact
import com.vestrel00.contacts.util.rawContacts
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.rawContact].
 */
suspend fun Insert.Result.rawContactAsync(
    context: Context, rawContact: MutableRawContact
): RawContact? = withContext(ASYNC_DISPATCHER) { rawContact(context, rawContact) { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.rawContacts].
 */
suspend fun Insert.Result.rawContactsAsync(context: Context): List<RawContact> =
    withContext(ASYNC_DISPATCHER) { rawContacts(context) { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.contact].
 */
suspend fun Insert.Result.contactAsync(context: Context, rawContact: MutableRawContact): Contact? =
    withContext(ASYNC_DISPATCHER) { contact(context, rawContact) { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.contacts].
 */
suspend fun Insert.Result.contactsAsync(context: Context): List<Contact> =
    withContext(ASYNC_DISPATCHER) { contacts(context) { !isActive } }