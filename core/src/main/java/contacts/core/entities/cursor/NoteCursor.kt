package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.NoteField

/**
 * Retrieves [Fields.Note] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class NoteCursor(cursor: Cursor, includeFields: Set<NoteField>) :
    AbstractDataCursor<NoteField>(cursor, includeFields) {

    val note: String? by string(Fields.Note.Note)
}
