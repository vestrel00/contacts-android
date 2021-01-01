package contacts.custom

import android.database.Cursor
import contacts.entities.cursor.DataCursor

/**
 * An abstract class that is used as a base of all custom common [DataCursor]s.
 *
 * ## Developer notes
 *
 * Technically, this can be optional. We could have implemented this part of the API to be able to
 * handle [DataCursor] directly instead of this [AbstractCustomCommonDataCursor]. However, we are
 * able to streamline all custom cursors this way, which makes our internal code easier to
 * follow / trace. It also gives us more control and flexibility.
 */
abstract class AbstractCustomCommonDataCursor(cursor: Cursor) : DataCursor(cursor)