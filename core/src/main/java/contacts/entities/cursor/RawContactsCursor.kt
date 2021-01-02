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

    override val contactId: Long? by long(RawContactsFields.ContactId)

    override val rawContactId: Long? by long(RawContactsFields.Id)

    val displayNamePrimary: String? by string(RawContactsFields.DisplayNamePrimary)

    val displayNameAlt: String? by string(RawContactsFields.DisplayNameAlt)

    val accountName: String? by string(RawContactsFields.AccountName)

    val accountType: String? by string(RawContactsFields.AccountType)

}

internal fun RawContactsCursor.account(): Account? {
    val name = accountName
    val type = accountType

    return if (name != null && type != null) Account(name, type) else null
}
