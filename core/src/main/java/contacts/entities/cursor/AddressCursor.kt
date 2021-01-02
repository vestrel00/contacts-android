package contacts.entities.cursor

import android.database.Cursor
import contacts.AddressField
import contacts.Fields
import contacts.entities.Address

/**
 * Retrieves [Fields.Address] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class AddressCursor(cursor: Cursor) : AbstractDataCursor<AddressField>(cursor) {

    val type: Address.Type?
        get() = Address.Type.fromValue(getInt(Fields.Address.Type))

    val label: String?
        get() = getString(Fields.Address.Label)

    val formattedAddress: String?
        get() = getString(Fields.Address.FormattedAddress)

    val street: String?
        get() = getString(Fields.Address.Street)

    val poBox: String?
        get() = getString(Fields.Address.PoBox)

    val neighborhood: String?
        get() = getString(Fields.Address.Neighborhood)

    val city: String?
        get() = getString(Fields.Address.City)

    val region: String?
        get() = getString(Fields.Address.Region)

    val postcode: String?
        get() = getString(Fields.Address.PostCode)

    val country: String?
        get() = getString(Fields.Address.Country)
}
