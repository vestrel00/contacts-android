package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.AbstractDataField
import contacts.core.Fields
import contacts.core.entities.Entity

/**
 * Retrieves [AbstractDataField] data from the given [cursor].
 */
internal class DataCursor(cursor: Cursor, includeFields: Set<AbstractDataField>?) :
    AbstractEntityCursor<AbstractDataField>(cursor, includeFields),
    DataIdCursor {

    override val dataId: Long by nonNullLong(Fields.DataId, Entity.INVALID_ID)

    override val rawContactId: Long by nonNullLong(Fields.RawContact.Id, Entity.INVALID_ID)

    override val contactId: Long by nonNullLong(Fields.Contact.Id, Entity.INVALID_ID)

    val isPrimary: Boolean by nonNullBoolean(Fields.IsPrimary)

    val isSuperPrimary: Boolean by nonNullBoolean(Fields.IsSuperPrimary)
}