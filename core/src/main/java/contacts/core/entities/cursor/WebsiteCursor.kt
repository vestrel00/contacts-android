package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.WebsiteField

/**
 * Retrieves [Fields.Website] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class WebsiteCursor(cursor: Cursor) : AbstractDataCursor<WebsiteField>(cursor) {

    val url: String? by string(Fields.Website.Url)
}
