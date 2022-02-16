package contacts.entities.custom.rpg.stats

import contacts.core.entities.custom.CustomDataRegistry.Entry
import contacts.entities.custom.rpg.RpgFields
import contacts.entities.custom.rpg.RpgMimeType
import contacts.entities.custom.rpg.RpgStatsField

// Keep this internal. Consumers don't need to see this stuff. Less visibility the better!
internal class RpgStatsEntry : Entry<RpgStatsField, RpgStatsDataCursor, RpgStatsEntity, RpgStats> {

    override val mimeType = RpgMimeType.Stats

    override val fieldSet = RpgFields.Stats

    override val fieldMapper = RpgStatsFieldMapper()

    override val countRestriction = RPG_STATS_COUNT_RESTRICTION

    override val mapperFactory = RpgStatsMapperFactory()

    override val operationFactory = RpgStatsOperationFactory()
}