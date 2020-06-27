package com.vestrel00.contacts.async.data

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.data.DataQuery
import com.vestrel00.contacts.entities.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.addresses].
 */
suspend fun DataQuery.addressesWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        List<Address> = withContext(context) { addresses { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.emails].
 */
suspend fun DataQuery.emailsWithContext(context: CoroutineContext = ASYNC_DISPATCHER): List<Email> =
    withContext(context) { emails { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.events].
 */
suspend fun DataQuery.eventsWithContext(context: CoroutineContext = ASYNC_DISPATCHER): List<Event> =
    withContext(context) { events { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.groupMemberships].
 */
suspend fun DataQuery.groupMembershipsWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        List<GroupMembership> = withContext(context) { groupMemberships { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.ims].
 */
suspend fun DataQuery.imsWithContext(context: CoroutineContext = ASYNC_DISPATCHER): List<Im> =
    withContext(context) { ims { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.names].
 */
suspend fun DataQuery.namesWithContext(context: CoroutineContext = ASYNC_DISPATCHER): List<Name> =
    withContext(context) { names { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.nicknames].
 */
suspend fun DataQuery.nicknamesWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        List<Nickname> = withContext(context) { nicknames { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.notes].
 */
suspend fun DataQuery.notesWithContext(context: CoroutineContext = ASYNC_DISPATCHER): List<Note> =
    withContext(context) { notes { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.organizations].
 */
suspend fun DataQuery.organizationsWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        List<Organization> = withContext(context) { organizations { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.phones].
 */
suspend fun DataQuery.phonesWithContext(context: CoroutineContext = ASYNC_DISPATCHER): List<Phone> =
    withContext(context) { phones { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.relations].
 */
suspend fun DataQuery.relationsWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        List<Relation> = withContext(context) { relations { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.sipAddresses].
 */
suspend fun DataQuery.sipAddressesWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        List<SipAddress> = withContext(context) { sipAddresses { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.websites].
 */
suspend fun DataQuery.websitesWithContext(context: CoroutineContext = ASYNC_DISPATCHER):
        List<Website> = withContext(context) { websites { !isActive } }

// endregion

// region ASYNC

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.addresses].
 */
fun DataQuery.addressesAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<List<Address>> = CoroutineScope(context).async { addresses { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.emails].
 */
fun DataQuery.emailsAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<List<Email>> =
    CoroutineScope(context).async { emails { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.events].
 */
fun DataQuery.eventsAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<List<Event>> =
    CoroutineScope(context).async { events { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.groupMemberships].
 */
fun DataQuery.groupMembershipsAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<List<GroupMembership>> =
    CoroutineScope(context).async { groupMemberships { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.ims].
 */
fun DataQuery.imsAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<List<Im>> =
    CoroutineScope(context).async { ims { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.names].
 */
fun DataQuery.namesAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<List<Name>> =
    CoroutineScope(context).async { names { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.nicknames].
 */
fun DataQuery.nicknamesAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<List<Nickname>> = CoroutineScope(context).async { nicknames { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.notes].
 */
fun DataQuery.notesAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<List<Note>> =
    CoroutineScope(context).async { notes { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.organizations].
 */
fun DataQuery.organizationsAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<List<Organization>> = CoroutineScope(context).async { organizations { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.phones].
 */
fun DataQuery.phonesAsync(context: CoroutineContext = ASYNC_DISPATCHER): Deferred<List<Phone>> =
    CoroutineScope(context).async { phones { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.relations].
 */
fun DataQuery.relationsAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<List<Relation>> = CoroutineScope(context).async { relations { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.sipAddresses].
 */
fun DataQuery.sipAddressesAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<List<SipAddress>> = CoroutineScope(context).async { sipAddresses { !isActive } }

/**
 * Suspends the current coroutine, performs the operation in the given [context], then returns the
 * result.
 *
 * Computations automatically stops if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.websites].
 */
fun DataQuery.websitesAsync(context: CoroutineContext = ASYNC_DISPATCHER):
        Deferred<List<Website>> = CoroutineScope(context).async { websites { !isActive } }

// endregion