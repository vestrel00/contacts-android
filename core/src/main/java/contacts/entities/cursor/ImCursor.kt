package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.entities.Im

/**
 * Retrieves [Fields.Im] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class ImCursor(cursor: Cursor) : DataCursor(cursor) {

    val protocol: Im.Protocol?
        get() = Im.Protocol.fromValue(cursor.getInt(Fields.Im.Protocol))

    val customProtocol: String?
        get() = cursor.getString(Fields.Im.CustomProtocol)

    val data: String?
        get() = cursor.getString(Fields.Im.Data)
}
