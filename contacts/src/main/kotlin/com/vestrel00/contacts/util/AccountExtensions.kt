package com.vestrel00.contacts.util

import android.accounts.Account
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.accounts.AccountsQuery

/**
 * Verifies that [this] given [Account] is in the list of all accounts in the system and returns
 * itself. Otherwise, returns null.
 */
internal fun Account.nullIfNotInSystem(context: Context): Account? =
    nullIfNotIn(AccountsQuery(context).allAccounts())

/**
 * Verifies that [this] given [Account] is in the list of given [accounts] and returns itself.
 * Otherwise, returns null.
 */
internal fun Account.nullIfNotIn(accounts: List<Account>): Account? =
    if (accounts.contains(this)) this else null

/**
 * Uses [whereOr] to form a where clause that matches any of the given [Account]s.
 *
 * If the sequence is empty, returns null.
 */
internal fun Sequence<Account?>.toRawContactsWhere(): Where? = distinct() // get rid of duplicates
    .whereOr { account ->
        if (account != null) {
            (RawContactsFields.AccountName equalToIgnoreCase account.name) and
                    (RawContactsFields.AccountType equalToIgnoreCase account.type)
        } else {
            RawContactsFields.AccountName.isNull() and
                    RawContactsFields.AccountType.isNull()
        }
    }