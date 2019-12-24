package com.vestrel00.contacts.util

import android.accounts.Account
import android.content.Context
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.entities.INVALID_ID
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.entities.cursor.getString
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo

fun RawContact.account(context: Context): Account? = accountForRawContactWithId(id, context)

fun MutableRawContact.account(context: Context): Account? = accountForRawContactWithId(id, context)

internal fun accountForRawContactWithId(rawContactId: Long, context: Context): Account? {
    if (!ContactsPermissions(context).canQuery() || rawContactId == INVALID_ID) {
        return null
    }

    val cursor = context.contentResolver.query(
        Table.RAW_CONTACTS.uri,
        Include(Fields.RawContact.AccountName, Fields.RawContact.AccountType).columnNames,
        "${Fields.RawContact.Id equalTo rawContactId}",
        null,
        null
    )

    if (cursor != null && cursor.moveToNext()) {
        val accountName = cursor.getString(Fields.RawContact.AccountName)
        val accountType = cursor.getString(Fields.RawContact.AccountType)

        if (accountName != null && accountType != null) {
            return Account(accountName, accountType)
        }
    }
    cursor?.close()

    return null
}