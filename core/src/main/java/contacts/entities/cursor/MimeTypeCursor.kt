package contacts.entities.cursor

import android.database.Cursor
import contacts.DataField
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.custom.CustomDataRegistry

/**
 * Retrieves [Fields.MimeType] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class MimeTypeCursor(cursor: Cursor, private val customDataRegistry: CustomDataRegistry) :
    AbstractEntityCursor<DataField>(cursor) {

    val mimeType: MimeType
        get() = MimeType.fromValue(getString(Fields.MimeType), customDataRegistry)
}
