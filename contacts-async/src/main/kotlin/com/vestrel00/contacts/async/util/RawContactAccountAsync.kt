package com.vestrel00.contacts.async.util

import android.accounts.Account
import android.content.Context
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.util.account
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * See [RawContact.account].
 */
suspend fun RawContact.accountAsync(context: Context): Account? =
    withContext(ASYNC_DISPATCHER) { account(context) }

/**
 * Suspends the current coroutine, performs the query operation in background, then returns the
 * control flow to the calling coroutine scope.
 *
 * See [MutableRawContact.account].
 */
suspend fun MutableRawContact.accountAsync(context: Context): Account? =
    withContext(ASYNC_DISPATCHER) { account(context) }