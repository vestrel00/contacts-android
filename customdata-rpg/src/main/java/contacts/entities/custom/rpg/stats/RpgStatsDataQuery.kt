package contacts.entities.custom.rpg.stats

import contacts.core.data.DataQuery
import contacts.core.data.DataQueryFactory
import contacts.entities.custom.rpg.RpgMimeType
import contacts.entities.custom.rpg.RpgStatsField
import contacts.entities.custom.rpg.RpgStatsFields

/**
 * Queries for [RpgStats]s.
 */
fun DataQueryFactory.rpgStats(): DataQuery<RpgStatsField, RpgStatsFields, RpgStats> =
    customData(RpgMimeType.Stats)