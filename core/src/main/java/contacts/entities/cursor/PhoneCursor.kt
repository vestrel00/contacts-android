package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.PhoneField
import contacts.entities.Phone

/**
 * Retrieves [Fields.Phone] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class PhoneCursor(cursor: Cursor) : AbstractDataCursor<PhoneField>(cursor) {

    val type: Phone.Type? by type(Fields.Phone.Type, typeFromValue = Phone.Type::fromValue)

    val label: String? by string(Fields.Phone.Label)

    val number: String? by string(Fields.Phone.Number)

    val normalizedNumber: String? by string(Fields.Phone.NormalizedNumber)
}
