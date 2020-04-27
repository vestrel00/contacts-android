package com.vestrel00.contacts.util

import android.accounts.Account
import com.vestrel00.contacts.*

/**
 * Uses [whereOr] to form a where clause that matches any of the given [Account]s.
 *
 * If the sequence is empty, returns null.
 */
internal fun Sequence<Account?>.toRawContactsWhere(): Where? = distinct() // don't allow duplicates
    .whereOr { account ->
        if (account != null) {
            (Fields.RawContacts.AccountName equalToIgnoreCase account.name) and
                    (Fields.RawContacts.AccountType equalToIgnoreCase account.type)
        } else {
            Fields.RawContacts.AccountName.isNull() and
                    Fields.RawContacts.AccountType.isNull()
        }
    }