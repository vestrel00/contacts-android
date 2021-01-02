package contacts.entities.cursor

import android.accounts.Account
import android.database.Cursor
import contacts.RawContactsField
import contacts.RawContactsFields

/**
 * Retrieves [RawContactsFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class RawContactsCursor(cursor: Cursor) : AbstractCursor<RawContactsField>(cursor),
    RawContactIdCursor {

    override val contactId: Long?
        get() = getLong(RawContactsFields.ContactId)

    override val rawContactId: Long?
        get() = getLong(RawContactsFields.Id)

    val displayNamePrimary: String?
        get() = getString(RawContactsFields.DisplayNamePrimary)

    val displayNameAlt: String?
        get() = getString(RawContactsFields.DisplayNameAlt)

    val accountName: String?
        get() = getString(RawContactsFields.AccountName)

    val accountType: String?
        get() = getString(RawContactsFields.AccountType)

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
