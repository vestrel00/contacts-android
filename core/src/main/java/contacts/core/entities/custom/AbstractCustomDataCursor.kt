package contacts.core.entities.custom

import android.database.Cursor
import contacts.core.AbstractDataField
import contacts.core.entities.cursor.AbstractDataCursor
import contacts.core.entities.cursor.DataCursor

/**
 * Base type of all custom [DataCursor]s. The type [T] are the fields allowed to be used in the
 * functions defined in [contacts.core.entities.cursor.AbstractEntityCursor].
 *
 * ## Include fields
 *
 * Data whose corresponding field is not specified in [includeFields] are guaranteed to be returned
 * as null even if the value in the database in not null. If [includeFields] is null, then the
 * included field checks are disabled. This means that any non-null data will be returned as is
 * (not null). This is a more optimal, recommended way of including all fields.
 */
abstract class AbstractCustomDataCursor<T : AbstractDataField>(
    cursor: Cursor, includeFields: Set<T>?
) : AbstractDataCursor<T>(cursor, includeFields)