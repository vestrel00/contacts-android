package contacts.entities.custom.gender

import contacts.core.data.DataQuery
import contacts.core.data.DataQueryFactory

/**
 * Queries for [Gender]s.
 */
fun DataQueryFactory.genders(): DataQuery<GenderField, GenderFields, Gender> =
    customData(GenderMimeType)