package contacts.entities.custom.gender

import contacts.data.CommonDataQuery
import contacts.data.DataQuery

/**
 * Queries for [Gender]s.
 */
fun DataQuery.genders(): CommonDataQuery<GenderField, Gender> = customData(GenderMimeType)