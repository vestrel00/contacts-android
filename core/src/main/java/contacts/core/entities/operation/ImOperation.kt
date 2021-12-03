package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.ImField
import contacts.core.entities.ImEntity
import contacts.core.entities.MimeType

internal class ImOperation(isProfile: Boolean, includeFields: Set<ImField>) :
    AbstractDataOperation<ImField, ImEntity>(isProfile, includeFields) {

    override val mimeType = MimeType.Im

    override fun setValuesFromData(
        data: ImEntity, setValue: (field: ImField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Im.Protocol, data.protocol?.value)
        setValue(Fields.Im.CustomProtocol, data.customProtocol)
        setValue(Fields.Im.Data, data.data)
    }
}