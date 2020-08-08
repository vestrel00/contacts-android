package com.vestrel00.contacts.util

import android.accounts.Account
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.accounts.AccountsQuery

/**
 * Returns true if [this] is in the list of all accounts in the system.
 *
 * ## Permissions
 *
 * Requires [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 */
internal fun Account.isInSystem(context: Context): Boolean = nullIfNotInSystem(context) != null

/**
 * Returns true if [this] is NOT in the list of all accounts in the system.
 *
 * ## Permissions
 *
 * Requires [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 */
internal fun Account.isNotInSystem(context: Context): Boolean = !isInSystem(context)

/**
 * Verifies that [this] given [Account] is in the list of all accounts in the system and returns
 * itself. Otherwise, returns null.
 *
 * ## Permissions
 *
 * Requires [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 */
internal fun Account.nullIfNotInSystem(context: Context): Account? =
    nullIfNotIn(AccountsQuery(context).allAccounts())

/**
 * Verifies that [this] given [Account] is in the list of given [accounts] and returns itself.
 * Otherwise, returns null.
 */
internal fun Account.nullIfNotIn(accounts: List<Account>): Account? =
    if (accounts.contains(this)) this else null

/*
 * A note about toRawContactsWhere and toGroupsWhere.
 *
 * Yes, I know that the column names of RawContactsFields and GroupsFields AccountName and
 * AccountType are the same. Two different functions exist that have outputs with the same
 * underlying data for adding type to the Where.
 *
 * - Output of toRawContactsWhere may be combined with other RawContactsFields.
 * - Output of toGroupsWhere may be combined with other GroupsFields.
 */

/**
 * Uses [whereOr] to form a where clause that matches the given [Account]. This is for use in
 * RawContacts table queries.
 */
internal fun Account?.toRawContactsWhere(): Where<RawContactsField> =
    // Assume that this will not return a null Where because there is one element in the sequence.
    sequenceOf(this).toRawContactsWhere() as Where<RawContactsField>

/**
 * Uses [whereOr] to form a where clause that matches any of the given [Account]s. This is for use
 * in RawContacts table queries.
 *
 * If the sequence is empty, returns null.
 */
internal fun Sequence<Account?>.toRawContactsWhere(): Where<RawContactsField>? = distinct()
    .whereOr { account ->
        if (account != null) {
            (RawContactsFields.AccountName equalToIgnoreCase account.name) and
                    (RawContactsFields.AccountType equalToIgnoreCase account.type)
        } else {
            RawContactsFields.AccountName.isNull() and
                    RawContactsFields.AccountType.isNull()
        }
    }

/**
 * Uses [whereOr] to form a where clause that matches any of the given [Account]s. This is for use
 * in Groups table queries.
 *
 * If the sequence is empty, returns null.
 */
internal fun Sequence<Account?>.toGroupsWhere(): Where<GroupsField>? = distinct()
    .whereOr { account ->
        if (account != null) {
            // RawContactsFields and GroupsFields AccountName and AccountType are the same.
            (GroupsFields.AccountName equalToIgnoreCase account.name) and
                    (GroupsFields.AccountType equalToIgnoreCase account.type)
        } else {
            GroupsFields.AccountName.isNull() and
                    GroupsFields.AccountType.isNull()
        }
    }