package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.SimContactsField
import contacts.core.SimContactsFields
import contacts.core.entities.Entity

/**
 * Retrieves [SimContactsFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class SimContactCursor(cursor: Cursor, includeFields: Set<SimContactsField>) :
    AbstractEntityCursor<SimContactsField>(cursor, includeFields) {

    val id: Long by nonNullLong(SimContactsFields.Id, Entity.INVALID_ID)

    val name: String? by string(SimContactsFields.Name)

    val number: String? by string(SimContactsFields.Number)
}
