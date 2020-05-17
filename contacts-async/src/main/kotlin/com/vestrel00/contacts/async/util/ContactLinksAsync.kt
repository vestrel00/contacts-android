package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.ContactEntity
import com.vestrel00.contacts.util.ContactLinkResult
import com.vestrel00.contacts.util.ContactUnlinkResult
import com.vestrel00.contacts.util.link
import com.vestrel00.contacts.util.unlink
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [ContactEntity.link].
 */
suspend fun ContactEntity.linkAsync(
    context: Context,
    vararg contacts: ContactEntity
): ContactLinkResult = linkAsync(context, contacts.asSequence())


/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [ContactEntity.link].
 */
suspend fun ContactEntity.linkAsync(
    context: Context,
    contacts: Collection<ContactEntity>
): ContactLinkResult = linkAsync(context, contacts.asSequence())

/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [ContactEntity.link].
 */
suspend fun ContactEntity.linkAsync(
    context: Context,
    contacts: Sequence<ContactEntity>
): ContactLinkResult = withContext(ASYNC_DISPATCHER) { link(context, contacts) }

/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [ContactEntity.link].
 */
suspend fun Collection<ContactEntity>.linkAsync(context: Context): ContactLinkResult =
    asSequence().linkAsync(context)

/**
 * Suspends the current coroutine, links the contacts, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [ContactEntity.link].
 */
suspend fun Sequence<ContactEntity>.linkAsync(context: Context): ContactLinkResult =
    withContext(ASYNC_DISPATCHER) { link(context) }

/**
 * Suspends the current coroutine, unlinks the contact, then returns the control flow to the calling
 * coroutine scope.
 *
 * See [ContactEntity.unlink].
 */
suspend fun ContactEntity.unlinkAsync(context: Context): ContactUnlinkResult =
    withContext(ASYNC_DISPATCHER) { unlink(context) }