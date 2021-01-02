package contacts.entities.cursor

import android.database.Cursor
import contacts.AbstractDataField
import contacts.Fields

/**
 * Retrieves [AbstractDataField] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class DataCursor(cursor: Cursor) : AbstractCursor<AbstractDataField>(cursor), DataIdCursor {

    override val dataId: Long?
        get() = getLong(Fields.DataId)

    override val rawContactId: Long?
        get() = getLong(Fields.RawContact.Id)

    override val contactId: Long?
        get() = getLong(Fields.Contact.Id)

    val isPrimary: Boolean
        get() = getBoolean(Fields.IsPrimary) ?: false

    val isSuperPrimary: Boolean
        get() = getBoolean(Fields.IsSuperPrimary) ?: false
}