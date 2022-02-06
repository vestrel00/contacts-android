package contacts.entities.custom.googlecontacts.userdefined

import contacts.core.data.DataQuery
import contacts.core.data.DataQueryFactory
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType
import contacts.entities.custom.googlecontacts.UserDefinedField
import contacts.entities.custom.googlecontacts.UserDefinedFields

/**
 * Queries for [UserDefined]s.
 */
fun DataQueryFactory.userDefined(): DataQuery<UserDefinedField, UserDefinedFields, UserDefined> =
    customData(GoogleContactsMimeType.UserDefined)