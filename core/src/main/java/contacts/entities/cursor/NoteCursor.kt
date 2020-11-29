package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields

/**
 * Retrieves [Fields.Note] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class NoteCursor(cursor: Cursor) : DataCursor(cursor) {

    val note: String?
        get() = cursor.getString(Fields.Note.Note)
}
