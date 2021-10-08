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
internal class DataContactsCursor(cursor: Cursor) : AbstractDataCursor<DataContactsField>(cursor),
    JoinedContactsCursor {

    override val contactId: Long? by long(Fields.Contact.Id)

    override val displayNamePrimary: String? by string(Fields.Contact.DisplayNamePrimary)

    override val displayNameAlt: String? by string(Fields.Contact.DisplayNameAlt)

    override val lastUpdatedTimestamp: Date? by date(Fields.Contact.LastUpdatedTimestamp)

    override val photoUri: Uri? by uri(Fields.Contact.PhotoUri)

    override val photoThumbnailUri: Uri? by uri(Fields.Contact.PhotoThumbnailUri)
}