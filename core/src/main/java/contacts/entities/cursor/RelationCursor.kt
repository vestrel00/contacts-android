package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.entities.Relation

/**
 * Retrieves [Fields.Relation] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class RelationCursor(cursor: Cursor) : DataCursor(cursor) {

    val type: Relation.Type?
        get() = Relation.Type.fromValue(cursor.getInt(Fields.Relation.Type))

    val label: String?
        get() = cursor.getString(Fields.Relation.Label)

    val name: String?
        get() = cursor.getString(Fields.Relation.Name)
}
