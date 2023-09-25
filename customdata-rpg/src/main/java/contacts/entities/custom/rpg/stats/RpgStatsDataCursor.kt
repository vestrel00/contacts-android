package contacts.entities.custom.rpg.stats

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataCursor
import contacts.entities.custom.rpg.RpgFields
import contacts.entities.custom.rpg.RpgStatsField

internal class RpgStatsDataCursor(cursor: Cursor, includeFields: Set<RpgStatsField>?) :
    AbstractCustomDataCursor<RpgStatsField>(cursor, includeFields) {

    val level: Int? by int(RpgFields.Stats.Level)
    val speed: Int? by int(RpgFields.Stats.Speed)
    val strength: Int? by int(RpgFields.Stats.Strength)
    val intelligence: Int? by int(RpgFields.Stats.Intelligence)
    val luck: Int? by int(RpgFields.Stats.Luck)
}