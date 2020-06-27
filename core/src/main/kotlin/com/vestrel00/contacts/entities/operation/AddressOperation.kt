package com.vestrel00.contacts.entities.operation

import com.vestrel00.contacts.AbstractField
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableAddress

internal class AddressOperation : AbstractDataOperation<MutableAddress>() {

    override val mimeType = MimeType.ADDRESS

    override fun setData(
        data: MutableAddress, setValue: (field: AbstractField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Address.Type, data.type?.value)
        setValue(Fields.Address.Label, data.label)
        setValue(Fields.Address.FormattedAddress, data.formattedAddress)
        setValue(Fields.Address.Street, data.street)
        setValue(Fields.Address.PoBox, data.poBox)
        setValue(Fields.Address.Neighborhood, data.neighborhood)
        setValue(Fields.Address.City, data.city)
        setValue(Fields.Address.Region, data.region)
        setValue(Fields.Address.PostCode, data.postcode)
        setValue(Fields.Address.Country, data.country)
    }
}