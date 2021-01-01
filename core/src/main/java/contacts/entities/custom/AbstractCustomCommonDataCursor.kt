package contacts.entities.custom

import android.database.Cursor
import contacts.entities.cursor.DataCursor

/**
 * An abstract class that is used as a base of all custom common [DataCursor]s.
 */
abstract class AbstractCustomCommonDataCursor(cursor: Cursor) : DataCursor(cursor)