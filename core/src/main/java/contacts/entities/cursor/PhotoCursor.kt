package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields

/**
 * Retrieves [Fields.Photo] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class PhotoCursor(cursor: Cursor) : DataCursor(cursor) {

    val photoFileId: Long?
        get() {
            val value = cursor.getLong(Fields.Photo.PhotoFileId)
            // Sometimes the value will be zero instead of null but 0 is not a valid photo file id.
            return if (value != null && value > 0) value else null
        }

    val photoThumbnail: ByteArray?
        get() = cursor.getBlob(Fields.Photo.PhotoThumbnail)
}
