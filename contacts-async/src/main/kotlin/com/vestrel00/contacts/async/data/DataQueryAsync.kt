package com.vestrel00.contacts.async.data

import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.data.DataQuery
import com.vestrel00.contacts.entities.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.addresses].
 */
suspend fun DataQuery.addressesAsync(): List<Address> =
    withContext(ASYNC_DISPATCHER) { addresses { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.emails].
 */
suspend fun DataQuery.emailsAsync(): List<Email> =
    withContext(ASYNC_DISPATCHER) { emails { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.events].
 */
suspend fun DataQuery.eventsAsync(): List<Event> =
    withContext(ASYNC_DISPATCHER) { events { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.groupMemberships].
 */
suspend fun DataQuery.groupMembershipsAsync(): List<GroupMembership> =
    withContext(ASYNC_DISPATCHER) { groupMemberships { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.ims].
 */
suspend fun DataQuery.imsAsync(): List<Im> = withContext(ASYNC_DISPATCHER) { ims { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.names].
 */
suspend fun DataQuery.namesAsync(): List<Name> =
    withContext(ASYNC_DISPATCHER) { names { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.nicknames].
 */
suspend fun DataQuery.nicknamesAsync(): List<Nickname> =
    withContext(ASYNC_DISPATCHER) { nicknames { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.notes].
 */
suspend fun DataQuery.notesAsync(): List<Note> =
    withContext(ASYNC_DISPATCHER) { notes { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.organizations].
 */
suspend fun DataQuery.organizationsAsync(): List<Organization> =
    withContext(ASYNC_DISPATCHER) { organizations { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.phones].
 */
suspend fun DataQuery.phonesAsync(): List<Phone> =
    withContext(ASYNC_DISPATCHER) { phones { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.relations].
 */
suspend fun DataQuery.relationsAsync(): List<Relation> =
    withContext(ASYNC_DISPATCHER) { relations { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.sipAddresses].
 */
suspend fun DataQuery.sipAddressesAsync(): List<SipAddress> =
    withContext(ASYNC_DISPATCHER) { sipAddresses { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [DataQuery.websites].
 */
suspend fun DataQuery.websitesAsync(): List<Website> =
    withContext(ASYNC_DISPATCHER) { websites { !isActive } }