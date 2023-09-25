package contacts.entities.custom.rpg.profession

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataCursor
import contacts.entities.custom.rpg.RpgFields
import contacts.entities.custom.rpg.RpgProfessionField

internal class RpgProfessionDataCursor(cursor: Cursor, includeFields: Set<RpgProfessionField>?) :
    AbstractCustomDataCursor<RpgProfessionField>(cursor, includeFields) {

    val title: String? by string(RpgFields.Profession.Title)
}