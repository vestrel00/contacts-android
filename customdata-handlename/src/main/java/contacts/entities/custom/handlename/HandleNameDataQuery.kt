package contacts.entities.custom.handlename

import contacts.core.data.CommonDataQuery
import contacts.core.data.DataQuery

/**
 * Queries for [HandleName]s.
 */
fun DataQuery.handleNames(): CommonDataQuery<HandleNameField, HandleName> =
    customData(HandleNameMimeType)