package contacts.entities.custom.gender

import android.database.Cursor
import contacts.entities.custom.AbstractCustomDataCursor

internal class GenderDataCursor(cursor: Cursor) : AbstractCustomDataCursor<GenderField>(cursor) {

    val type: Gender.Type? by type(GenderFields.Type, typeFromValue = Gender.Type::fromValue)

    val label: String? by string(GenderFields.Label)
}