package contacts.entities.custom.handlename

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataCursor

internal class HandleNameDataCursor(cursor: Cursor, includeFields: Set<HandleNameField>) :
    AbstractCustomDataCursor<HandleNameField>(cursor, includeFields) {

    val handle: String? by string(HandleNameFields.Handle)
}