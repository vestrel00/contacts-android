package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.AbstractDataField
import contacts.core.Fields

/**
 * Retrieves [AbstractDataField] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class DataCursor(cursor: Cursor, includeFields: Set<AbstractDataField>) :
    AbstractEntityCursor<AbstractDataField>(cursor, includeFields),
    DataIdCursor {

    override val dataId: Long? by long(Fields.DataId)

    override val rawContactId: Long? by long(Fields.RawContact.Id)

    override val contactId: Long? by long(Fields.Contact.Id)

    val isPrimary: Boolean by nonNullBoolean(Fields.IsPrimary)

    val isSuperPrimary: Boolean by nonNullBoolean(Fields.IsSuperPrimary)
}