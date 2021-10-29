package contacts.core.entities.cursor

import android.net.Uri
import java.util.*

/**
 * Contacts table fields that are also available in the Data table.
 *
 * The [OptionsCursor]'s underlying field values are the same across the the Contacts, RawContacts,
 * and Data table. However, it is not the case here for one particular field (the contact id) in
 * this set of fields;
 *
 * - Contacts table: "_id"
 * - Data table: "contact_id"
 *
 * See [ContactIdCursor].
 */
internal interface JoinedContactsCursor : ContactIdCursor {

    val displayNamePrimary: String?

    val displayNameAlt: String?

    val lastUpdatedTimestamp: Date?

    val photoUri: Uri?

    val photoThumbnailUri: Uri?

    val hasPhoneNumber: Boolean?
}