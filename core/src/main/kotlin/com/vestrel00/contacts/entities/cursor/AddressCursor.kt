package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.Address

/**
 * Retrieves [Fields.Address] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class AddressCursor(cursor: Cursor) : DataCursor(cursor) {

    val type: Address.Type?
        get() = Address.Type.fromValue(cursor.getInt(Fields.Address.Type))

    val label: String?
        get() = cursor.getString(Fields.Address.Label)

    val formattedAddress: String?
        get() = cursor.getString(Fields.Address.FormattedAddress)

    val street: String?
        get() = cursor.getString(Fields.Address.Street)

    val poBox: String?
        get() = cursor.getString(Fields.Address.PoBox)

    val neighborhood: String?
        get() = cursor.getString(Fields.Address.Neighborhood)

    val city: String?
        get() = cursor.getString(Fields.Address.City)

    val region: String?
        get() = cursor.getString(Fields.Address.Region)

    val postcode: String?
        get() = cursor.getString(Fields.Address.PostCode)

    val country: String?
        get() = cursor.getString(Fields.Address.Country)
}
