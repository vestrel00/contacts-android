package contacts.entities.custom.rpg.profession

import contacts.core.data.DataQuery
import contacts.core.data.DataQueryFactory
import contacts.entities.custom.rpg.RpgMimeType
import contacts.entities.custom.rpg.RpgProfessionField
import contacts.entities.custom.rpg.RpgProfessionFields

/**
 * Queries for [RpgProfession]s.
 */
fun DataQueryFactory.rpgProfession(): DataQuery<RpgProfessionField, RpgProfessionFields, RpgProfession> =
    customData(RpgMimeType.Profession)