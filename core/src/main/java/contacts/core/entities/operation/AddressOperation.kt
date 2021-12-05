package contacts.core.entities.operation

import contacts.core.AddressField
import contacts.core.Fields
import contacts.core.entities.AddressEntity
import contacts.core.entities.MimeType

internal class AddressOperation(isProfile: Boolean, includeFields: Set<AddressField>) :
    AbstractDataOperation<AddressField, AddressEntity>(isProfile, includeFields) {

    override val mimeType = MimeType.Address

    override fun setValuesFromData(
        data: AddressEntity, setValue: (field: AddressField, dataValue: Any?) -> Unit
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