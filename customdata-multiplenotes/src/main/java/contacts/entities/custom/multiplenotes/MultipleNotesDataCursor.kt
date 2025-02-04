package contacts.entities.custom.multiplenotes

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataCursor

internal class MultipleNotesDataCursor(cursor: Cursor, includeFields: Set<MultipleNotesField>?) :
    AbstractCustomDataCursor<MultipleNotesField>(cursor, includeFields) {

    val note: String? by string(MultipleNotesFields.Note)
}