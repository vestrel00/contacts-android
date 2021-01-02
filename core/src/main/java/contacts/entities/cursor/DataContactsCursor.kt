package contacts.entities.cursor

import android.database.Cursor
import contacts.DataContactsField
import contacts.Fields
import java.util.*

/**
 * Retrieves [Fields.Contact] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class DataContactsCursor(cursor: Cursor) : AbstractDataCursor<DataContactsField>(cursor),
    JoinedContactsCursor {

    override val contactId: Long?
        get() = getLong(Fields.Contact.Id)

    override val displayNamePrimary: String?
        get() = getString(Fields.Contact.DisplayNamePrimary)

    override val displayNameAlt: String?
        get() = getString(Fields.Contact.DisplayNameAlt)

    override val lastUpdatedTimestamp: Date?
        get() = getDate(Fields.Contact.LastUpdatedTimestamp)
}