package com.vestrel00.contacts.entities.cursor

import android.accounts.Account
import android.database.Cursor
import com.vestrel00.contacts.Fields

/**
 * Retrieves [Fields.Groups] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class GroupsCursor(private val cursor: Cursor) {

    val id: Long?
        get() = cursor.getLong(Fields.Groups.Id)

    val title: String
        get() = cursor.getString(Fields.Groups.Title) ?: "Null"

    val readOnly: Boolean
        get() = cursor.getBoolean(Fields.Groups.ReadOnly) ?: false

    val favorites: Boolean
        get() = cursor.getBoolean(Fields.Groups.Favorites) ?: false

    val autoAdd: Boolean
        get() = cursor.getBoolean(Fields.Groups.AutoAdd) ?: false

    val account: Account
        get() {
            // There should never be null account name or type...
            val accountName = cursor.getString(Fields.Groups.AccountName) ?: "null"
            val accountType = cursor.getString(Fields.Groups.AccountType) ?: "null"

            return Account(accountName, accountType)
        }
}
