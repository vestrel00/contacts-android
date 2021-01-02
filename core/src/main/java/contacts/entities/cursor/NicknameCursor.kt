package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.NicknameField

/**
 * Retrieves [Fields.Nickname] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class NicknameCursor(cursor: Cursor) : AbstractDataCursor<NicknameField>(cursor) {

    val name: String?
        get() = getString(Fields.Nickname.Name)
}
