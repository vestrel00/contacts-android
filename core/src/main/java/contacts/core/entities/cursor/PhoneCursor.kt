package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.PhoneField
import contacts.core.entities.PhoneEntity

/**
 * Retrieves [Fields.Phone] data from the given [cursor].
 */
internal class PhoneCursor(cursor: Cursor, includeFields: Set<PhoneField>?) :
    AbstractDataCursor<PhoneField>(cursor, includeFields) {

    val type: PhoneEntity.Type? by type(
        Fields.Phone.Type,
        typeFromValue = PhoneEntity.Type::fromValue
    )

    val label: String? by string(Fields.Phone.Label)

    val number: String? by string(Fields.Phone.Number)

    val normalizedNumber: String? by string(Fields.Phone.NormalizedNumber)
}
