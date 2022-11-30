package contacts.async.util

import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.aggregationexceptions.ContactLink
import contacts.core.aggregationexceptions.ContactUnlink
import contacts.core.entities.ExistingContactEntity
import contacts.core.util.linkDirect
import contacts.core.util.unlinkDirect
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
 * See [ExistingContactEntity.linkDirect].
 */
suspend fun ExistingContactEntity.linkDirectWithContext(
    contactsApi: Contacts,
    vararg contacts: ExistingContactEntity,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkDirectWithContext(contactsApi, contacts.asSequence(), coroutineContext = coroutineContext)


/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingContactEntity.linkDirect].
 */
suspend fun ExistingContactEntity.linkDirectWithContext(
    contactsApi: Contacts,
    contacts: Collection<ExistingContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkDirectWithContext(contactsApi, contacts.asSequence(), coroutineContext = coroutineContext)

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingContactEntity.linkDirect].
 */
suspend fun ExistingContactEntity.linkDirectWithContext(
    contactsApi: Contacts,
    contacts: Sequence<ExistingContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactLink.Result = withContext(coroutineContext) { linkDirect(contactsApi, contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingContactEntity.linkDirect].
 */
suspend fun Collection<ExistingContactEntity>.linkDirectWithContext(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactLink.Result = asSequence().linkDirectWithContext(contactsApi, coroutineContext)

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingContactEntity.linkDirect].
 */
suspend fun Sequence<ExistingContactEntity>.linkDirectWithContext(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactLink.Result = withContext(coroutineContext) { linkDirect(contactsApi) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingContactEntity.unlinkDirect].
 */
suspend fun ExistingContactEntity.unlinkDirectWithContext(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ContactUnlink.Result = withContext(coroutineContext) { unlinkDirect(contactsApi) }

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.linkDirect].
 */
fun ExistingContactEntity.linkDirectAsync(
    contactsApi: Contacts,
    vararg contacts: ExistingContactEntity,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkDirectAsync(contactsApi, contacts.asSequence(), coroutineContext = coroutineContext)


/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.linkDirect].
 */
fun ExistingContactEntity.linkDirectAsync(
    contactsApi: Contacts,
    contacts: Collection<ExistingContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
) = linkDirectAsync(contactsApi, contacts.asSequence(), coroutineContext = coroutineContext)

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.linkDirect].
 */
fun ExistingContactEntity.linkDirectAsync(
    contactsApi: Contacts,
    contacts: Sequence<ExistingContactEntity>,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactLink.Result> =
    CoroutineScope(coroutineContext).async { linkDirect(contactsApi, contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.linkDirect].
 */
fun Collection<ExistingContactEntity>.linkDirectAsync(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactLink.Result> = asSequence().linkDirectAsync(contactsApi, coroutineContext)

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.linkDirect].
 */
fun Sequence<ExistingContactEntity>.linkDirectAsync(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactLink.Result> = CoroutineScope(coroutineContext).async { linkDirect(contactsApi) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.unlinkDirect].
 */
fun ExistingContactEntity.unlinkDirectAsync(
    contactsApi: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ContactUnlink.Result> =
    CoroutineScope(coroutineContext).async { unlinkDirect(contactsApi) }

// endregion