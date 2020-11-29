package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.util.ContactLinkResult
import com.vestrel00.contacts.util.ContactUnlinkResult
import com.vestrel00.contacts.util.contact
import com.vestrel00.contacts.util.contacts
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ContactLinkResult.contact].
 */
suspend fun ContactLinkResult.contactWithContext(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) { contact(context) { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ContactUnlinkResult.contacts].
 */
suspend fun ContactUnlinkResult.contactsWithContext(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<Contact> = withContext(coroutineContext) { contacts(context) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ContactLinkResult.contact].
 */
fun ContactLinkResult.contactAsync(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> = CoroutineScope(coroutineContext).async { contact(context) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ContactUnlinkResult.contacts].
 */
fun ContactUnlinkResult.contactsAsync(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<Contact>> =
    CoroutineScope(coroutineContext).async { contacts(context) { !isActive } }