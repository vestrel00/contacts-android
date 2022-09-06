package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.PhoneLookupField
import contacts.core.PhoneLookupFields
import contacts.core.entities.Entity

/**
 * Retrieves [PhoneLookupFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class PhoneLookupCursor(
    cursor: Cursor, includeFields: Set<PhoneLookupField>
) : AbstractEntityCursor<PhoneLookupField>(cursor, includeFields) {

    val id: Long by nonNullLong(PhoneLookupFields.Id, Entity.INVALID_ID)

    val contactId: Long by nonNullLong(PhoneLookupFields.ContactId, Entity.INVALID_ID)
}
