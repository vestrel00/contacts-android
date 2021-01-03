package contacts.entities.operation

import contacts.Fields
import contacts.ImField
import contacts.entities.MimeType
import contacts.entities.MutableIm

internal class ImOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<ImField, MutableIm>(isProfile) {

    override val mimeType = MimeType.Im

    override fun setData(
        data: MutableIm, setValue: (field: ImField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Im.Protocol, data.protocol?.value)
        setValue(Fields.Im.CustomProtocol, data.customProtocol)
        setValue(Fields.Im.Data, data.data)
    }
}