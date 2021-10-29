package contacts.core.entities.operation

import contacts.core.EmailField
import contacts.core.Fields
import contacts.core.entities.MimeType
import contacts.core.entities.MutableEmail

internal class EmailOperation(isProfile: Boolean, includeFields: Set<EmailField>) :
    AbstractCommonDataOperation<EmailField, MutableEmail>(isProfile, includeFields) {

    override val mimeType = MimeType.Email

    override fun setData(
        data: MutableEmail, setValue: (field: EmailField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Email.Type, data.type?.value)
        setValue(Fields.Email.Label, data.label)
        setValue(Fields.Email.Address, data.address)
    }
}