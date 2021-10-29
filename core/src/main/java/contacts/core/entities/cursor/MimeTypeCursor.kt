package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.DataField
import contacts.core.Fields
import contacts.core.entities.MimeType
import contacts.core.entities.custom.CustomDataRegistry

/**
 * Retrieves [Fields.MimeType] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class MimeTypeCursor(cursor: Cursor, private val customDataRegistry: CustomDataRegistry) :
    AbstractEntityCursor<DataField>(cursor, setOf(Fields.MimeType)) {

    val mimeType: MimeType
        get() = MimeType.fromValue(getString(Fields.MimeType), customDataRegistry)
}
