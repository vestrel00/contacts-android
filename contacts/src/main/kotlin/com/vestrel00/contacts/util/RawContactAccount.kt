package com.vestrel00.contacts.util

import android.accounts.Account
import android.content.Context
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.entities.cursor.rawContactsCursor
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo

/**
 * Queries the RawContacts table and returns the [Account] this [RawContact] belongs to.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION]! This will return null if permission is
 * not granted (or if there is no associated account).
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.account(context: Context): Account? = accountForRawContactWithId(id, context)

/**
 * Queries the RawContacts table and returns the [Account] this [RawContact] belongs to.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION]! This will return null if permission is
 * not granted (or if there is no associated account).
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.account(context: Context): Account? = accountForRawContactWithId(id, context)

internal fun accountForRawContactWithId(rawContactId: Long?, context: Context): Account? {
    if (!ContactsPermissions(context).canQuery() || rawContactId == null) {
        return null
    }

    return context.contentResolver.query(
        Table.RAW_CONTACTS.uri,
        Include(Fields.RawContacts.AccountName, Fields.RawContacts.AccountType),
        Fields.RawContacts.Id equalTo rawContactId
    ) {
        var account: Account? = null

        if (it.moveToNext()) {
            val rawContactsCursor = it.rawContactsCursor()
            val accountName = rawContactsCursor.accountName
            val accountType = rawContactsCursor.accountType

            if (accountName != null && accountType != null) {
                account = Account(accountName, accountType)
            }
        }

        account
    }
}