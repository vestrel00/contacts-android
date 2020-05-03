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

// GET ACCOUNT

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
        if (it.moveToNext()) {
            val rawContactsCursor = it.rawContactsCursor()
            val accountName = rawContactsCursor.accountName
            val accountType = rawContactsCursor.accountType

            if (accountName != null && accountType != null) {
                Account(accountName, accountType)
            } else {
                null
            }
        } else {
            null
        }
    }
}

// SET ACCOUNT

// TODO Implement; Account.associateRawContacts and (Mutable)RawContact.associateWithAccount
// Contacts Provider automatically creates a group membership to the default group of the target Account when the account changes.
//     - This occurs even if the group membership already exists resulting in duplicates.
// Contacts Provider DOES NOT delete existing group memberships when the account changes.
//     - This has to be done manually to prevent duplicates to the default group.
// For Lollipop (API 22) and below, the Contacts Provider sets null accounts to non-null asynchronously.
//     - Just add a note about this behavior.
// Update DEV_NOTES data required and groups / group membership sections.

