package contacts.entities.operation

import contacts.Field
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableAddress

internal class AddressOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableAddress>(isProfile) {

    override val mimeType = MimeType.Address

    override fun setData(
        data: MutableAddress, setValue: (field: Field, dataValue: Any?) -> Unit
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