package com.vestrel00.contacts.entities.cursor

import android.accounts.Account
import android.database.Cursor
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.INVALID_ID

/**
 * Retrieves [Fields.Group] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class GroupCursor(private val cursor: Cursor) {

    val id: Long
        get() = cursor.getLong(Fields.Group.Id) ?: INVALID_ID

    val title: String
        get() = cursor.getString(Fields.Group.Title) ?: "Null"

    val readOnly: Boolean
        get() = cursor.getBoolean(Fields.Group.ReadOnly) ?: false

    val favorites: Boolean
        get() = cursor.getBoolean(Fields.Group.Favorites) ?: false

    val autoAdd: Boolean
        get() = cursor.getBoolean(Fields.Group.AutoAdd) ?: false

    val account: Account
        get() {
            // There should never be null account name or type...
            val accountName = cursor.getString(Fields.Group.AccountName) ?: "null"
            val accountType = cursor.getString(Fields.Group.AccountType) ?: "null"

            return Account(accountName, accountType)
        }
}
