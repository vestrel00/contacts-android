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

    val displayName: String? by string(Fields.Name.DisplayName)

    val givenName: String? by string(Fields.Name.GivenName)

    val middleName: String? by string(Fields.Name.MiddleName)

    val familyName: String? by string(Fields.Name.FamilyName)

    val prefix: String? by string(Fields.Name.Prefix)

    val suffix: String? by string(Fields.Name.Suffix)

    val phoneticGivenName: String? by string(Fields.Name.PhoneticGivenName)

    val phoneticMiddleName: String? by string(Fields.Name.PhoneticMiddleName)

    val phoneticFamilyName: String? by string(Fields.Name.PhoneticFamilyName)
}
