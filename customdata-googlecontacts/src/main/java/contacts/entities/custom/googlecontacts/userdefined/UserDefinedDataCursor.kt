package contacts.entities.custom.googlecontacts.userdefined

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataCursor
import contacts.entities.custom.googlecontacts.GoogleContactsFields
import contacts.entities.custom.googlecontacts.UserDefinedField

internal class UserDefinedDataCursor(cursor: Cursor, includeFields: Set<UserDefinedField>?) :
    AbstractCustomDataCursor<UserDefinedField>(cursor, includeFields) {

    val field: String? by string(GoogleContactsFields.UserDefined.Field)
    val label: String? by string(GoogleContactsFields.UserDefined.Label)
}