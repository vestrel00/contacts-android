package com.vestrel00.contacts.async.accounts

import android.accounts.Account
import com.vestrel00.contacts.accounts.AccountsQuery
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.RawContactEntity
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsQuery.accountFor].
 */
suspend fun AccountsQuery.accountForAsync(rawContact: RawContactEntity): Account? =
    withContext(ASYNC_DISPATCHER) { accountFor(rawContact) }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsQuery.accountsFor].
 */
suspend fun AccountsQuery.accountsForAsync(vararg rawContacts: RawContactEntity):
        AccountsQuery.AccountsList = accountsForAsync(rawContacts.asSequence())

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsQuery.accountsFor].
 */
suspend fun AccountsQuery.accountsForAsync(rawContacts: Collection<RawContactEntity>):
        AccountsQuery.AccountsList = accountsForAsync(rawContacts.asSequence())

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * Automatically gets cancelled if the parent coroutine scope / job is cancelled.
 *
 * See [AccountsQuery.accountsFor].
 */
suspend fun AccountsQuery.accountsForAsync(rawContacts: Sequence<RawContactEntity>):
        AccountsQuery.AccountsList = withContext(ASYNC_DISPATCHER) {
    accountsFor(rawContacts) { !isActive }
}