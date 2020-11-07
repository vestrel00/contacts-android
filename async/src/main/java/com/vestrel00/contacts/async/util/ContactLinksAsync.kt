package com.vestrel00.contacts.async.util

import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.ContactEntity
import com.vestrel00.contacts.util.ContactLinkResult
import com.vestrel00.contacts.util.ContactUnlinkResult
import com.vestrel00.contacts.util.link
import com.vestrel00.contacts.util.unlink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.link].
 */
suspend fun ContactEntity.linkWithContext(
    context: Context,
    vararg contacts: ContactEntity,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkWithContext(context, contacts.asSequence(), coroutineContext = coroutineContext)


/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.link].
 */
suspend fun ContactEntity.linkWithContext(
    context: Context,
    contacts: Collection<ContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkWithContext(context, contacts.asSequence(), coroutineContext = coroutineContext)

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.link].
 */
suspend fun ContactEntity.linkWithContext(
    context: Context,
    contacts: Sequence<ContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactLinkResult = withContext(coroutineContext) { link(context, contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.link].
 */
suspend fun Collection<ContactEntity>.linkWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactLinkResult = asSequence().linkWithContext(context, coroutineContext)

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.link].
 */
suspend fun Sequence<ContactEntity>.linkWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactLinkResult = withContext(coroutineContext) { link(context) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.unlink].
 */
suspend fun ContactEntity.unlinkWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactUnlinkResult = withContext(coroutineContext) { unlink(context) }

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.link].
 */
fun ContactEntity.linkAsync(
    context: Context,
    vararg contacts: ContactEntity,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkAsync(context, contacts.asSequence(), coroutineContext = coroutineContext)


/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.link].
 */
fun ContactEntity.linkAsync(
    context: Context,
    contacts: Collection<ContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkAsync(context, contacts.asSequence(), coroutineContext = coroutineContext)

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.link].
 */
fun ContactEntity.linkAsync(
    context: Context,
    contacts: Sequence<ContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactLinkResult> = CoroutineScope(coroutineContext).async { link(context, contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.link].
 */
fun Collection<ContactEntity>.linkAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactLinkResult> = asSequence().linkAsync(context, coroutineContext)

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.link].
 */
fun Sequence<ContactEntity>.linkAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactLinkResult> = CoroutineScope(coroutineContext).async { link(context) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.unlink].
 */
fun ContactEntity.unlinkAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactUnlinkResult> = CoroutineScope(coroutineContext).async { unlink(context) }

// endregion