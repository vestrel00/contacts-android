package com.vestrel00.contacts.entities.cursor

import android.accounts.Account
import android.database.Cursor
import com.vestrel00.contacts.GroupsFields

/**
 * Retrieves [GroupsFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class GroupsCursor(private val cursor: Cursor) {

    val id: Long?
        get() = cursor.getLong(GroupsFields.Id)

    val title: String
        get() = cursor.getString(GroupsFields.Title) ?: "Null"

    val readOnly: Boolean
        get() = cursor.getBoolean(GroupsFields.ReadOnly) ?: false

    val favorites: Boolean
        get() = cursor.getBoolean(GroupsFields.Favorites) ?: false

    val autoAdd: Boolean
        get() = cursor.getBoolean(GroupsFields.AutoAdd) ?: false

    val account: Account
        get() {
            // There should never be null account name or type...
            val accountName = cursor.getString(GroupsFields.AccountName) ?: "null"
            val accountType = cursor.getString(GroupsFields.AccountType) ?: "null"

            return Account(accountName, accountType)
        }
}
