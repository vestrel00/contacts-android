package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.custom.CustomDataRegistry

/**
 * Retrieves [Fields.MimeType] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class MimeTypeCursor(
    private val cursor: Cursor,
    private val customDataRegistry: CustomDataRegistry
) {

    val mimeType: MimeType
        get() = MimeType.fromValue(cursor.getString(Fields.MimeType), customDataRegistry)
}
