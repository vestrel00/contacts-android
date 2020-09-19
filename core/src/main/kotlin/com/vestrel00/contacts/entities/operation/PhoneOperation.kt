package com.vestrel00.contacts.entities.operation

import com.vestrel00.contacts.Field
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutablePhone

internal class PhoneOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutablePhone>(isProfile) {

    override val mimeType = MimeType.PHONE

    override fun setData(
        data: MutablePhone, setValue: (field: Field, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Phone.Type, data.type?.value)
        setValue(Fields.Phone.Label, data.label)
        setValue(Fields.Phone.Number, data.number)
        setValue(Fields.Phone.NormalizedNumber, data.normalizedNumber)
    }
}