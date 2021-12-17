package contacts.core.entities.cursor

import android.accounts.Account
import android.database.Cursor
import contacts.core.RawContactsField
import contacts.core.RawContactsFields
import contacts.core.entities.Entity

/**
 * Retrieves [RawContactsFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class RawContactsCursor(cursor: Cursor, includeFields: Set<RawContactsField>) :
    AbstractEntityCursor<RawContactsField>(cursor, includeFields), RawContactIdCursor {

    override val contactId: Long by nonNullLong(RawContactsFields.ContactId, Entity.INVALID_ID)

    override val rawContactId: Long by nonNullLong(RawContactsFields.Id, Entity.INVALID_ID)

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
