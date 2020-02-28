package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.util.ContactLinkResult
import com.vestrel00.contacts.util.contact
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext


/**
 * Suspends the current coroutine, gets the contact, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [ContactLinkResult.contact].
 */
suspend fun ContactLinkResult.contactAsync(context: Context):
        Contact? = withContext(ASYNC_DISPATCHER) { contact(context) { !isActive } }