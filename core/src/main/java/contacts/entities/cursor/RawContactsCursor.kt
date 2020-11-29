package contacts.entities.cursor

import android.accounts.Account
import android.database.Cursor
import contacts.RawContactsFields

/**
 * Retrieves [RawContactsFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class RawContactsCursor(private val cursor: Cursor) : RawContactIdCursor {

    override val contactId: Long?
        get() = cursor.getLong(RawContactsFields.ContactId)

    override val rawContactId: Long?
        get() = cursor.getLong(RawContactsFields.Id)

    val displayNamePrimary: String?
        get() = cursor.getString(RawContactsFields.DisplayNamePrimary)

    val displayNameAlt: String?
        get() = cursor.getString(RawContactsFields.DisplayNameAlt)

    val accountName: String?
        get() = cursor.getString(RawContactsFields.AccountName)

    val accountType: String?
        get() = cursor.getString(RawContactsFields.AccountType)

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
