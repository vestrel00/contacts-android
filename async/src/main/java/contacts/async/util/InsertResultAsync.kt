package contacts.async.util

import android.content.Context
import contacts.async.ASYNC_DISPATCHER
import contacts.core.Insert
import contacts.core.entities.Contact
import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.custom.GlobalCustomDataRegistry
import contacts.core.util.contact
import contacts.core.util.contacts
import contacts.core.util.rawContact
import contacts.core.util.rawContacts
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.rawContact].
 */
suspend fun Insert.Result.rawContactWithContext(
    context: Context,
    rawContact: MutableRawContact,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): RawContact? = withContext(coroutineContext) {
    rawContact(context, rawContact, customDataRegistry) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.rawContacts].
 */
suspend fun Insert.Result.rawContactsWithContext(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<RawContact> = withContext(coroutineContext) {
    rawContacts(context, customDataRegistry) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.contact].
 */
suspend fun Insert.Result.contactWithContext(
    context: Context,
    rawContact: MutableRawContact,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Contact? = withContext(coroutineContext) {
    contact(context, rawContact, customDataRegistry) { !isActive }
}

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.contacts].
 */
suspend fun Insert.Result.contactsWithContext(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): List<Contact> = withContext(coroutineContext) {
    contacts(context, customDataRegistry) { !isActive }
}

// endregion

// region ASYNC

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.rawContact].
 */
fun Insert.Result.rawContactAsync(
    context: Context,
    rawContact: MutableRawContact,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<RawContact?> = CoroutineScope(coroutineContext).async {
    rawContact(context, rawContact, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.rawContacts].
 */
fun Insert.Result.rawContactsAsync(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<RawContact>> = CoroutineScope(coroutineContext).async {
    rawContacts(context, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.contact].
 */
fun Insert.Result.contactAsync(
    context: Context,
    rawContact: MutableRawContact,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Contact?> = CoroutineScope(coroutineContext).async {
    contact(context, rawContact, customDataRegistry) { !isActive }
}

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [Insert.Result.contacts].
 */
fun Insert.Result.contactsAsync(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<List<Contact>> = CoroutineScope(coroutineContext).async {
    contacts(context, customDataRegistry) { !isActive }
}

// endregion