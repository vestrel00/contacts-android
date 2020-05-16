package com.vestrel00.contacts.entities.cursor

import android.accounts.Account
import android.database.Cursor
import com.vestrel00.contacts.Fields

/**
 * Retrieves [Fields.RawContacts] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class RawContactsCursor(private val cursor: Cursor) : RawContactIdCursor {

    override val contactId: Long?
        get() = cursor.getLong(Fields.RawContacts.ContactId)

    override val rawContactId: Long?
        get() = cursor.getLong(Fields.RawContacts.Id)

    val displayName: String?
        get() = cursor.getString(Fields.RawContacts.DisplayName)

    val accountName: String?
        get() = cursor.getString(Fields.RawContacts.AccountName)

    val accountType: String?
        get() = cursor.getString(Fields.RawContacts.AccountType)

}

internal fun RawContactsCursor.account(): Account? {
    val name = accountName
    val type = accountType

    return if (name != null && type != null) {
        Account(name, type)
    } else {
        null
    }
}
