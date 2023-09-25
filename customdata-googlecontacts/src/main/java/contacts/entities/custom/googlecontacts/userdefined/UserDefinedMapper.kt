package contacts.entities.custom.googlecontacts.userdefined

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataEntityMapper
import contacts.entities.custom.googlecontacts.UserDefinedField

internal class UserDefinedMapperFactory :
    AbstractCustomDataEntityMapper.Factory<UserDefinedField, UserDefinedDataCursor, UserDefined> {

    override fun create(
        cursor: Cursor, includeFields: Set<UserDefinedField>?
    ): AbstractCustomDataEntityMapper<UserDefinedField, UserDefinedDataCursor, UserDefined> =
        UserDefinedMapper(UserDefinedDataCursor(cursor, includeFields))
}

private class UserDefinedMapper(cursor: UserDefinedDataCursor) :
    AbstractCustomDataEntityMapper<UserDefinedField, UserDefinedDataCursor, UserDefined>(cursor) {

    override fun value(cursor: UserDefinedDataCursor) = UserDefined(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        field = cursor.field,
        label = cursor.label,

        isRedacted = false
    )
}