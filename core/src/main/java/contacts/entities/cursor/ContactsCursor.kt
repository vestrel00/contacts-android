package contacts.entities.cursor

import android.annotation.TargetApi
import android.database.Cursor
import android.net.Uri
import android.os.Build
import contacts.ContactsFields
import java.util.*

/**
 * Retrieves [ContactsFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class ContactsCursor(private val cursor: Cursor) : JoinedContactsCursor {

    override val contactId: Long?
        get() = cursor.getLong(ContactsFields.Id)

    override val displayNamePrimary: String?
        get() = cursor.getString(ContactsFields.DisplayNamePrimary)

    override val displayNameAlt: String?
        get() = cursor.getString(ContactsFields.DisplayNameAlt)

    override val lastUpdatedTimestamp: Date?
        get() = cursor.getDate(ContactsFields.LastUpdatedTimestamp)

    val displayNameSource: Int?
        get() = cursor.getInt(ContactsFields.DisplayNameSource)

    val nameRawContactId: Long?
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        get() = cursor.getLong(ContactsFields.NameRawContactId)

    val photoUri: Uri?
        get() = cursor.getUri(ContactsFields.PhotoUri)

    val photoThumbnailUri: Uri?
        get() = cursor.getUri(ContactsFields.PhotoThumbnailUri)

    val photoFileId: Long?
        get() {
            val value = cursor.getLong(ContactsFields.PhotoFileId)
            // Sometimes the value will be zero instead of null but 0 is not a valid photo file id.
            return if (value != null && value > 0) value else null
        }
}
