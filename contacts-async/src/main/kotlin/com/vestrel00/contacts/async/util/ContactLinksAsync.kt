package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.util.ContactLinkResult
import com.vestrel00.contacts.util.link
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [Contact.link].
 */
suspend fun Contact.linkAsync(context: Context, vararg contacts: Contact): ContactLinkResult =
    withContext(ASYNC_DISPATCHER) { link(context, contacts.asSequence()) }


/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [Contact.link].
 */
suspend fun Contact.linkAsync(context: Context, contacts: Collection<Contact>): ContactLinkResult =
    withContext(ASYNC_DISPATCHER) { link(context, contacts.asSequence()) }

/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [Contact.link].
 */
suspend fun Contact.linkAsync(context: Context, contacts: Sequence<Contact>): ContactLinkResult =
    withContext(ASYNC_DISPATCHER) { link(context, contacts) }

/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [Contact.link].
 */
suspend fun MutableContact.linkAsync(context: Context, vararg contacts: MutableContact):
        ContactLinkResult = withContext(ASYNC_DISPATCHER) { link(context, contacts.asSequence()) }


/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [Contact.link].
 */
suspend fun MutableContact.linkAsync(context: Context, contacts: Collection<MutableContact>):
        ContactLinkResult = withContext(ASYNC_DISPATCHER) { link(context, contacts.asSequence()) }

/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [Contact.link].
 */
suspend fun MutableContact.linkAsync(context: Context, contacts: Sequence<MutableContact>):
        ContactLinkResult = withContext(ASYNC_DISPATCHER) { link(context, contacts) }