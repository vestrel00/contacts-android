package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableName
import com.vestrel00.contacts.entities.Name
import com.vestrel00.contacts.entities.cursor.NameCursor

internal class NameMapper(private val nameCursor: NameCursor) : EntityMapper<Name, MutableName> {

    override val toImmutable: Name
        get() = Name(
            id = nameCursor.id,
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

    override val toMutable: MutableName
        get() = MutableName(
            id = nameCursor.id,
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
