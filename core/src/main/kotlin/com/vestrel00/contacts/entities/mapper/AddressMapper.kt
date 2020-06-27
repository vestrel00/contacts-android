package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Address
import com.vestrel00.contacts.entities.cursor.AddressCursor

internal class AddressMapper(private val addressCursor: AddressCursor) : EntityMapper<Address> {

    override val value: Address
        get() = Address(
            id = addressCursor.dataId,
            rawContactId = addressCursor.rawContactId,
            contactId = addressCursor.contactId,

            isPrimary = addressCursor.isPrimary,
            isSuperPrimary = addressCursor.isSuperPrimary,

            type = addressCursor.type,
            label = addressCursor.label,

            formattedAddress = addressCursor.formattedAddress,
            street = addressCursor.street,
            poBox = addressCursor.poBox,
            neighborhood = addressCursor.neighborhood,
            city = addressCursor.city,
            region = addressCursor.region,
            postcode = addressCursor.postcode,
            country = addressCursor.country
        )
}
