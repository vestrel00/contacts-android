package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.WebsiteField

/**
 * Retrieves [Fields.Website] data from the given [cursor].
 */
internal class WebsiteCursor(cursor: Cursor, includeFields: Set<WebsiteField>?) :
    AbstractDataCursor<WebsiteField>(cursor, includeFields) {

    val url: String? by string(Fields.Website.Url)
}
