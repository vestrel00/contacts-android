package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields

open class DataCursor(protected val cursor: Cursor) : DataIdCursor {

    override val dataId: Long?
        get() = cursor.getLong(Fields.DataId)

    override val rawContactId: Long?
        get() = cursor.getLong(Fields.RawContact.Id)

    override val contactId: Long?
        get() = cursor.getLong(Fields.Contact.Id)

    val isPrimary: Boolean
        get() = cursor.getBoolean(Fields.IsPrimary) ?: false

    val isSuperPrimary: Boolean
        get() = cursor.getBoolean(Fields.IsSuperPrimary) ?: false
}