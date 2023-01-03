package contacts.core.entities.cursor

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
    AbstractEntityCursor<RawContactsField>(cursor, includeFields), AccountCursor,
    RawContactIdCursor {

    override val accountName: String? by string(RawContactsFields.AccountName)

    override val accountType: String? by string(RawContactsFields.AccountType)

    override val contactId: Long by nonNullLong(RawContactsFields.ContactId, Entity.INVALID_ID)

    override val rawContactId: Long by nonNullLong(RawContactsFields.Id, Entity.INVALID_ID)

    val displayNamePrimary: String? by string(RawContactsFields.DisplayNamePrimary)

    val displayNameAlt: String? by string(RawContactsFields.DisplayNameAlt)

}