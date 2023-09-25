package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.NoteField

/**
 * Retrieves [Fields.Note] data from the given [cursor].
 */
internal class NoteCursor(cursor: Cursor, includeFields: Set<NoteField>?) :
    AbstractDataCursor<NoteField>(cursor, includeFields) {

    val note: String? by string(Fields.Note.Note)
}
