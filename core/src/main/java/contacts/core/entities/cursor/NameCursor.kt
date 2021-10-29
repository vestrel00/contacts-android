package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.NameField

/**
 * Retrieves [Fields.Name] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class NameCursor(cursor: Cursor, includeFields: Set<NameField>) :
    AbstractDataCursor<NameField>(cursor, includeFields) {

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
