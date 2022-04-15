package contacts.core.entities.cursor

import android.database.Cursor
import android.net.Uri
import contacts.core.DataContactsField
import contacts.core.Fields
import java.util.*

/**
 * Retrieves [Fields.Contact] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class DataContactsCursor(cursor: Cursor, includeFields: Set<DataContactsField>) :
    AbstractDataCursor<DataContactsField>(cursor, includeFields), JoinedContactsCursor {

    override val lookupKey: String? by string(Fields.Contact.LookupKey)

    override val displayNamePrimary: String? by string(Fields.Contact.DisplayNamePrimary)

    override val displayNameAlt: String? by string(Fields.Contact.DisplayNameAlt)

    override val lastUpdatedTimestamp: Date? by date(Fields.Contact.LastUpdatedTimestamp)

    override val photoFileId: Long?
        get() {
            val value = getLong(Fields.Contact.PhotoFileId)
            // Sometimes the value will be zero instead of null but 0 is not a valid photo file id.
            return if (value != null && value > 0) value else null
        }

    override val photoUri: Uri? by uri(Fields.Contact.PhotoUri)

    override val photoThumbnailUri: Uri? by uri(Fields.Contact.PhotoThumbnailUri)

    override val hasPhoneNumber: Boolean? by boolean(Fields.Contact.HasPhoneNumber)
}