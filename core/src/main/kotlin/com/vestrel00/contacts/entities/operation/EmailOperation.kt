package com.vestrel00.contacts.entities.operation

import com.vestrel00.contacts.Field
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableEmail

internal class EmailOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableEmail>(isProfile) {

    override val mimeType = MimeType.EMAIL

    override fun setData(
        data: MutableEmail, setValue: (field: Field, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Email.Type, data.type?.value)
        setValue(Fields.Email.Label, data.label)
        setValue(Fields.Email.Address, data.address)
    }
}