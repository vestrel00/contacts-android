package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields

/**
 * Retrieves [Fields.Name] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class NameCursor(cursor: Cursor) : DataCursor(cursor) {

    val displayName: String?
        get() = cursor.getString(Fields.Name.DisplayName)

    val givenName: String?
        get() = cursor.getString(Fields.Name.GivenName)

    val middleName: String?
        get() = cursor.getString(Fields.Name.MiddleName)

    val familyName: String?
        get() = cursor.getString(Fields.Name.FamilyName)

    val prefix: String?
        get() = cursor.getString(Fields.Name.Prefix)

    val suffix: String?
        get() = cursor.getString(Fields.Name.Suffix)

    val phoneticGivenName: String?
        get() = cursor.getString(Fields.Name.PhoneticGivenName)

    val phoneticMiddleName: String?
        get() = cursor.getString(Fields.Name.PhoneticMiddleName)

    val phoneticFamilyName: String?
        get() = cursor.getString(Fields.Name.PhoneticFamilyName)
}
