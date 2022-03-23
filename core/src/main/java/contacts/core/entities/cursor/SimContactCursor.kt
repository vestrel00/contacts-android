package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.SimContactField
import contacts.core.SimContactFields
import contacts.core.entities.Entity

/**
 * Retrieves [SimContactFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class SimContactCursor(cursor: Cursor, includeFields: Set<SimContactField>) :
    AbstractEntityCursor<SimContactField>(cursor, includeFields) {

    val id: Long by nonNullLong(SimContactFields.Id, Entity.INVALID_ID)

    val name: String? by string(SimContactFields.Name)

    val number: String? by string(SimContactFields.Number)

    val email: String? by string(SimContactFields.Email)
}
