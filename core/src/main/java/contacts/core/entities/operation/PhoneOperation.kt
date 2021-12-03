package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.PhoneField
import contacts.core.entities.MimeType
import contacts.core.entities.PhoneEntity

internal class PhoneOperation(isProfile: Boolean, includeFields: Set<PhoneField>) :
    AbstractDataOperation<PhoneField, PhoneEntity>(isProfile, includeFields) {

    override val mimeType = MimeType.Phone

    override fun setValuesFromData(
        data: PhoneEntity, setValue: (field: PhoneField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Phone.Type, data.type?.value)
        setValue(Fields.Phone.Label, data.label)
        setValue(Fields.Phone.Number, data.number)
        setValue(Fields.Phone.NormalizedNumber, data.normalizedNumber)
    }
}