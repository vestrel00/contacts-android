package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.ContactEntity
import contacts.core.util.ContactLinkResult
import contacts.core.util.ContactUnlinkResult
import contacts.core.util.link
import contacts.core.util.unlink
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
    contactsApi: Contacts,
    vararg contacts: ContactEntity,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkWithContext(contactsApi, contacts.asSequence(), coroutineContext = coroutineContext)


/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.link].
 */
suspend fun ContactEntity.linkWithContext(
    contactsApi: Contacts,
    contacts: Collection<ContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkWithContext(contactsApi, contacts.asSequence(), coroutineContext = coroutineContext)

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.link].
 */
suspend fun ContactEntity.linkWithContext(
    contactsApi: Contacts,
    contacts: Sequence<ContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactLinkResult = withContext(coroutineContext) { link(contactsApi, contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.link].
 */
suspend fun Collection<ContactEntity>.linkWithContext(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactLinkResult = asSequence().linkWithContext(contactsApi, coroutineContext)

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.link].
 */
suspend fun Sequence<ContactEntity>.linkWithContext(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactLinkResult = withContext(coroutineContext) { link(contactsApi) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ContactEntity.unlink].
 */
suspend fun ContactEntity.unlinkWithContext(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactUnlinkResult = withContext(coroutineContext) { unlink(contactsApi) }

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.link].
 */
fun ContactEntity.linkAsync(
    contactsApi: Contacts,
    vararg contacts: ContactEntity,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkAsync(contactsApi, contacts.asSequence(), coroutineContext = coroutineContext)


/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.link].
 */
fun ContactEntity.linkAsync(
    contactsApi: Contacts,
    contacts: Collection<ContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkAsync(contactsApi, contacts.asSequence(), coroutineContext = coroutineContext)

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.link].
 */
fun ContactEntity.linkAsync(
    contactsApi: Contacts,
    contacts: Sequence<ContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactLinkResult> =
    CoroutineScope(coroutineContext).async { link(contactsApi, contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.link].
 */
fun Collection<ContactEntity>.linkAsync(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactLinkResult> = asSequence().linkAsync(contactsApi, coroutineContext)

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.link].
 */
fun Sequence<ContactEntity>.linkAsync(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactLinkResult> = CoroutineScope(coroutineContext).async { link(contactsApi) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.unlink].
 */
fun ContactEntity.unlinkAsync(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactUnlinkResult> = CoroutineScope(coroutineContext).async { unlink(contactsApi) }

// endregion