package com.vestrel00.contacts.async

import com.vestrel00.contacts.QueryData
import com.vestrel00.contacts.entities.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.addresses].
 */
suspend fun QueryData.addressesAsync(): List<Address> =
    withContext(ASYNC_DISPATCHER) { addresses { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.companies].
 */
suspend fun QueryData.companiesAsync(): List<Company> =
    withContext(ASYNC_DISPATCHER) { companies { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.emails].
 */
suspend fun QueryData.emailsAsync(): List<Email> =
    withContext(ASYNC_DISPATCHER) { emails { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.events].
 */
suspend fun QueryData.eventsAsync(): List<Event> =
    withContext(ASYNC_DISPATCHER) { events { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.groupMemberships].
 */
suspend fun QueryData.groupMembershipsAsync(): List<GroupMembership> =
    withContext(ASYNC_DISPATCHER) { groupMemberships { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.ims].
 */
suspend fun QueryData.imsAsync(): List<Im> = withContext(ASYNC_DISPATCHER) { ims { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.names].
 */
suspend fun QueryData.namesAsync(): List<Name> =
    withContext(ASYNC_DISPATCHER) { names { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.nicknames].
 */
suspend fun QueryData.nicknamesAsync(): List<Nickname> =
    withContext(ASYNC_DISPATCHER) { nicknames { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.notes].
 */
suspend fun QueryData.notesAsync(): List<Note> =
    withContext(ASYNC_DISPATCHER) { notes { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.phones].
 */
suspend fun QueryData.phonesAsync(): List<Phone> =
    withContext(ASYNC_DISPATCHER) { phones { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.relations].
 */
suspend fun QueryData.relationsAsync(): List<Relation> =
    withContext(ASYNC_DISPATCHER) { relations { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.sipAddresses].
 */
suspend fun QueryData.sipAddressesAsync(): List<SipAddress> =
    withContext(ASYNC_DISPATCHER) { sipAddresses { !isActive } }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [QueryData.websites].
 */
suspend fun QueryData.websitesAsync(): List<Website> =
    withContext(ASYNC_DISPATCHER) { websites { !isActive } }