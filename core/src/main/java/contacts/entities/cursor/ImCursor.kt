package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.ImField
import contacts.entities.Im

/**
 * Retrieves [Fields.Im] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class ImCursor(cursor: Cursor) : AbstractDataCursor<ImField>(cursor) {

    val protocol: Im.Protocol?
        get() = Im.Protocol.fromValue(getInt(Fields.Im.Protocol))

    val customProtocol: String?
        get() = getString(Fields.Im.CustomProtocol)

    val data: String?
        get() = getString(Fields.Im.Data)
}
