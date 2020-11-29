package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.CommonDataEntity
import com.vestrel00.contacts.util.contact
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [CommonDataEntity.contact].
 */
suspend fun CommonDataEntity.contactWithContext(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) { contact(context) { !isActive } }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [CommonDataEntity.contact].
 */
fun CommonDataEntity.contactAsync(
    context: Context, coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> =
    CoroutineScope(coroutineContext).async { contact(context) { !isActive } }