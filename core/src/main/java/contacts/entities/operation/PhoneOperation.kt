package contacts.entities.operation

import contacts.Fields
import contacts.PhoneField
import contacts.entities.MimeType
import contacts.entities.MutablePhone

internal class PhoneOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<PhoneField, MutablePhone>(isProfile) {

    override val mimeType = MimeType.Phone

    override fun setData(
        data: MutablePhone, setValue: (field: PhoneField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Phone.Type, data.type?.value)
        setValue(Fields.Phone.Label, data.label)
        setValue(Fields.Phone.Number, data.number)
        setValue(Fields.Phone.NormalizedNumber, data.normalizedNumber)
    }
}