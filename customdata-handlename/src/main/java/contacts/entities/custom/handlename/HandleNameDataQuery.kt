package contacts.entities.custom.handlename

import contacts.core.data.DataQuery
import contacts.core.data.DataQueryFactory

/**
 * Queries for [HandleName]s.
 */
fun DataQueryFactory.handleNames(): DataQuery<HandleNameField, HandleNameFields, HandleName> =
    customData(HandleNameMimeType)