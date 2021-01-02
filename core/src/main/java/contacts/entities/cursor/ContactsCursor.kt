package contacts.entities.cursor

import android.annotation.TargetApi
import android.database.Cursor
import android.net.Uri
import android.os.Build
import contacts.ContactsField
import contacts.ContactsFields
import java.util.*

/**
 * Retrieves [ContactsFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class ContactsCursor(cursor: Cursor) : AbstractCursor<ContactsField>(cursor),
    JoinedContactsCursor {

    override val contactId: Long?
        get() = getLong(ContactsFields.Id)

    override val displayNamePrimary: String?
        get() = getString(ContactsFields.DisplayNamePrimary)

    override val displayNameAlt: String?
        get() = getString(ContactsFields.DisplayNameAlt)

    override val lastUpdatedTimestamp: Date?
        get() = getDate(ContactsFields.LastUpdatedTimestamp)

    val displayNameSource: Int?
        get() = getInt(ContactsFields.DisplayNameSource)

    val nameRawContactId: Long?
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        get() = getLong(ContactsFields.NameRawContactId)

    val photoUri: Uri?
        get() = getUri(ContactsFields.PhotoUri)

    val photoThumbnailUri: Uri?
        get() = getUri(ContactsFields.PhotoThumbnailUri)

    val photoFileId: Long?
        get() {
            val value = getLong(ContactsFields.PhotoFileId)
            // Sometimes the value will be zero instead of null but 0 is not a valid photo file id.
            return if (value != null && value > 0) value else null
        }
}
