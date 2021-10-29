package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.PhoneField
import contacts.core.entities.Phone

/**
 * Retrieves [Fields.Phone] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class PhoneCursor(cursor: Cursor, includeFields: Set<PhoneField>) :
    AbstractDataCursor<PhoneField>(cursor, includeFields) {

    val type: Phone.Type? by type(Fields.Phone.Type, typeFromValue = Phone.Type::fromValue)

    val label: String? by string(Fields.Phone.Label)

    val number: String? by string(Fields.Phone.Number)

    val normalizedNumber: String? by string(Fields.Phone.NormalizedNumber)
}
