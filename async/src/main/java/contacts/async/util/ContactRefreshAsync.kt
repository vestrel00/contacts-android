package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.util.refresh
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Contact.refresh].
 */
suspend fun Contact.refreshWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) { refresh(context) { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Contact.refresh].
 */
suspend fun MutableContact.refreshWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): MutableContact? = withContext(coroutineContext) { refresh(context) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Contact.refresh].
 */
fun Contact.refreshAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> = CoroutineScope(coroutineContext).async { refresh(context) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Contact.refresh].
 */
fun MutableContact.refreshAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<MutableContact?> =
    CoroutineScope(coroutineContext).async { refresh(context) { !isActive } }