package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.ImField
import contacts.core.entities.MimeType
import contacts.core.entities.MutableIm

internal class ImOperation(isProfile: Boolean, includeFields: Set<ImField>) :
    AbstractDataOperation<ImField, MutableIm>(isProfile, includeFields) {

    override val mimeType = MimeType.Im

    override fun setData(
        data: MutableIm, setValue: (field: ImField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Im.Protocol, data.protocol?.value)
        setValue(Fields.Im.CustomProtocol, data.customProtocol)
        setValue(Fields.Im.Data, data.data)
    }
}