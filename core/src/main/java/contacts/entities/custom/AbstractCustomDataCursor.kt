package contacts.entities.custom

import android.database.Cursor
import contacts.AbstractDataField
import contacts.entities.cursor.AbstractDataCursor
import contacts.entities.cursor.DataCursor

/**
 * An abstract class that is used as a base of all custom [DataCursor]s. The type [T] are the fields
 * allowed to be used in the functions defined in [contacts.entities.cursor.AbstractCursor].
 */
abstract class AbstractCustomDataCursor<T : AbstractDataField>(cursor: Cursor) :
    AbstractDataCursor<T>(cursor)