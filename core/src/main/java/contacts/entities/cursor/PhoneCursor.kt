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

    val type: Phone.Type?
        get() = Phone.Type.fromValue(getInt(Fields.Phone.Type))

    val label: String?
        get() = getString(Fields.Phone.Label)

    val number: String?
        get() = getString(Fields.Phone.Number)

    val normalizedNumber: String?
        get() = getString(Fields.Phone.NormalizedNumber)
}
