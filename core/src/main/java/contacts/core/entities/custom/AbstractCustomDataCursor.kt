package contacts.core.entities.custom

import android.database.Cursor
import contacts.core.AbstractDataField
import contacts.core.entities.cursor.AbstractDataCursor
import contacts.core.entities.cursor.DataCursor

/**
 * An abstract class that is used as a base of all custom [DataCursor]s. The type [T] are the fields
 * allowed to be used in the functions defined in
 * [contacts.core.entities.cursor.AbstractEntityCursor].
 */
abstract class AbstractCustomDataCursor<T : AbstractDataField>(
    cursor: Cursor, includeFields: Set<T>
) : AbstractDataCursor<T>(cursor, includeFields)