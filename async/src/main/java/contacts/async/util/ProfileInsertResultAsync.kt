package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.profile.ProfileInsert
import com.vestrel00.contacts.util.contact
import com.vestrel00.contacts.util.rawContact
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.rawContact].
 */
suspend fun ProfileInsert.Result.rawContactWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): RawContact? = withContext(coroutineContext) { rawContact(context) { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.contact].
 */
suspend fun ProfileInsert.Result.contactWithContext(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) { contact(context) { !isActive } }

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.rawContact].
 */
fun ProfileInsert.Result.rawContactAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<RawContact?> =
    CoroutineScope(coroutineContext).async { rawContact(context) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [ProfileInsert.Result.contact].
 */
fun ProfileInsert.Result.contactAsync(
    context: Context,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> =
    CoroutineScope(coroutineContext).async { contact(context) { !isActive } }

// endregion