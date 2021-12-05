package contacts.core.entities.mapper

import contacts.core.entities.Name
import contacts.core.entities.cursor.NameCursor

internal class NameMapper(private val nameCursor: NameCursor) : DataEntityMapper<Name> {

    override val value: Name
        get() = Name(
            id = nameCursor.dataId,
            rawContactId = nameCursor.rawContactId,
            contactId = nameCursor.contactId,

            isPrimary = nameCursor.isPrimary,
            isSuperPrimary = nameCursor.isSuperPrimary,

            displayName = nameCursor.displayName,

            givenName = nameCursor.givenName,
            middleName = nameCursor.middleName,
            familyName = nameCursor.familyName,

            prefix = nameCursor.prefix,
            suffix = nameCursor.suffix,

            phoneticGivenName = nameCursor.phoneticGivenName,
            phoneticMiddleName = nameCursor.phoneticMiddleName,
            phoneticFamilyName = nameCursor.phoneticFamilyName
        )
}
