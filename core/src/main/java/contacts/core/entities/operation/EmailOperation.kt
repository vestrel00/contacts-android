package contacts.core.entities.operation

import contacts.core.EmailField
import contacts.core.Fields
import contacts.core.entities.EmailEntity
import contacts.core.entities.MimeType

internal class EmailOperation(isProfile: Boolean, includeFields: Set<EmailField>) :
    AbstractDataOperation<EmailField, EmailEntity>(isProfile, includeFields) {

    override val mimeType = MimeType.Email

    override fun setValuesFromData(
        data: EmailEntity, setValue: (field: EmailField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Email.Type, data.type?.value)
        setValue(Fields.Email.Label, data.label)
        setValue(Fields.Email.Address, data.address)
    }
}