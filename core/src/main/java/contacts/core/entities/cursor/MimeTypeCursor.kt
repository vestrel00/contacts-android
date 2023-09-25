package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.GenericDataField
import contacts.core.entities.MimeType
import contacts.core.entities.custom.CustomDataRegistry

/**
 * Retrieves [Fields.MimeType] data from the given [cursor].
 */
internal class MimeTypeCursor(cursor: Cursor, private val customDataRegistry: CustomDataRegistry) :
    AbstractEntityCursor<GenericDataField>(cursor, null) {

    val mimeType: MimeType
        get() = MimeType.fromValue(getString(Fields.MimeType), customDataRegistry)
}
