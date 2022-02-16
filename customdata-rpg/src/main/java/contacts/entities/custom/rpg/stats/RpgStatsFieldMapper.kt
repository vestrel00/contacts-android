package contacts.entities.custom.rpg.stats

import contacts.core.entities.custom.CustomDataFieldMapper
import contacts.entities.custom.rpg.RpgFields
import contacts.entities.custom.rpg.RpgStatsField

internal class RpgStatsFieldMapper : CustomDataFieldMapper<RpgStatsField, RpgStatsEntity> {

    override fun valueOf(field: RpgStatsField, customDataEntity: RpgStatsEntity): String? =
        when (field) {
            RpgFields.Stats.Level -> customDataEntity.level?.toString()
            RpgFields.Stats.Speed -> customDataEntity.speed?.toString()
            RpgFields.Stats.Strength -> customDataEntity.strength?.toString()
            RpgFields.Stats.Intelligence -> customDataEntity.intelligence?.toString()
            RpgFields.Stats.Luck -> customDataEntity.luck?.toString()
            else -> throw RpgStatsDataException("Unrecognized rpg stats field $field")
        }
}