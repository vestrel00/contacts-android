package contacts.entities.custom.rpg.stats

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataEntityMapper
import contacts.entities.custom.rpg.RpgStatsField

internal class RpgStatsMapperFactory :
    AbstractCustomDataEntityMapper.Factory<RpgStatsField, RpgStatsDataCursor, RpgStats> {

    override fun create(
        cursor: Cursor, includeFields: Set<RpgStatsField>?
    ): AbstractCustomDataEntityMapper<RpgStatsField, RpgStatsDataCursor, RpgStats> =
        RpgStatsMapper(RpgStatsDataCursor(cursor, includeFields))
}

private class RpgStatsMapper(cursor: RpgStatsDataCursor) :
    AbstractCustomDataEntityMapper<RpgStatsField, RpgStatsDataCursor, RpgStats>(cursor) {

    override fun value(cursor: RpgStatsDataCursor) = RpgStats(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        level = cursor.level,
        speed = cursor.speed,
        strength = cursor.strength,
        intelligence = cursor.intelligence,
        luck = cursor.luck,

        isRedacted = false
    )
}