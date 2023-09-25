package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.PhotoField

/**
 * Retrieves [Fields.Photo] data from the given [cursor].
 */
internal class PhotoCursor(cursor: Cursor, includeFields: Set<PhotoField>?) :
    AbstractDataCursor<PhotoField>(cursor, includeFields) {

    val photoFileId: Long?
        get() {
            val value = getLong(Fields.Photo.PhotoFileId)
            // Sometimes the value will be zero instead of null but 0 is not a valid photo file id.
            return if (value != null && value > 0) value else null
        }

    val photoThumbnail: ByteArray? by blob(Fields.Photo.PhotoThumbnail)
}
