package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.NameField

/**
 * Retrieves [Fields.Name] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class NameCursor(cursor: Cursor) : AbstractDataCursor<NameField>(cursor) {

    val displayName: String?
        get() = getString(Fields.Name.DisplayName)

    val givenName: String?
        get() = getString(Fields.Name.GivenName)

    val middleName: String?
        get() = getString(Fields.Name.MiddleName)

    val familyName: String?
        get() = getString(Fields.Name.FamilyName)

    val prefix: String?
        get() = getString(Fields.Name.Prefix)

    val suffix: String?
        get() = getString(Fields.Name.Suffix)

    val phoneticGivenName: String?
        get() = getString(Fields.Name.PhoneticGivenName)

    val phoneticMiddleName: String?
        get() = getString(Fields.Name.PhoneticMiddleName)

    val phoneticFamilyName: String?
        get() = getString(Fields.Name.PhoneticFamilyName)
}
