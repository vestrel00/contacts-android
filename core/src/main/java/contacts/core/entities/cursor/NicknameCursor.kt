package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.NicknameField

/**
 * Retrieves [Fields.Nickname] data from the given [cursor].
 */
internal class NicknameCursor(cursor: Cursor, includeFields: Set<NicknameField>?) :
    AbstractDataCursor<NicknameField>(cursor, includeFields) {

    val name: String? by string(Fields.Nickname.Name)
}
