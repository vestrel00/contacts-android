package contacts.entities.custom.handlename

import contacts.data.CommonDataQuery
import contacts.data.DataQuery

/**
 * Queries for [HandleName]s.
 */
fun DataQuery.handleNames(): CommonDataQuery<HandleNameField, HandleName> =
    customData(HandleNameMimeType)