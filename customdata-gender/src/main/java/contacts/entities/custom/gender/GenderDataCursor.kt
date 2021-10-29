package contacts.entities.custom.gender

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataCursor

internal class GenderDataCursor(cursor: Cursor, includeFields: Set<GenderField>) :
    AbstractCustomDataCursor<GenderField>(cursor, includeFields) {

    val type: Gender.Type? by type(GenderFields.Type, typeFromValue = Gender.Type::fromValue)

    val label: String? by string(GenderFields.Label)
}