package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.RelationField
import contacts.entities.Relation

/**
 * Retrieves [Fields.Relation] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class RelationCursor(cursor: Cursor) : AbstractDataCursor<RelationField>(cursor) {

    val type: Relation.Type? by type(Fields.Relation.Type, typeFromValue = Relation.Type::fromValue)

    val label: String? by string(Fields.Relation.Label)

    val name: String? by string(Fields.Relation.Name)
}
