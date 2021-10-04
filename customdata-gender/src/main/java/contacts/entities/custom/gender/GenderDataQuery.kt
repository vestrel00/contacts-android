package contacts.entities.custom.gender

import contacts.core.data.CommonDataQuery
import contacts.core.data.DataQuery

/**
 * Queries for [Gender]s.
 */
fun DataQuery.genders(): CommonDataQuery<GenderField, Gender> = customData(GenderMimeType)