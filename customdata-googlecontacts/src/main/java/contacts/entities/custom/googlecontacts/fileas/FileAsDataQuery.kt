package contacts.entities.custom.googlecontacts.fileas

import contacts.core.data.DataQuery
import contacts.core.data.DataQueryFactory
import contacts.entities.custom.googlecontacts.FileAsField
import contacts.entities.custom.googlecontacts.FileAsFields
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType

/**
 * Queries for [FileAs]s.
 */
fun DataQueryFactory.fileAs(): DataQuery<FileAsField, FileAsFields, FileAs> =
    customData(GoogleContactsMimeType.FileAs)