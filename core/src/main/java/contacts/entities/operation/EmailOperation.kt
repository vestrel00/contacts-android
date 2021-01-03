package contacts.entities.operation

import contacts.EmailField
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableEmail

internal class EmailOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<EmailField, MutableEmail>(isProfile) {

    override val mimeType = MimeType.Email

    override fun setData(
        data: MutableEmail, setValue: (field: EmailField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Email.Type, data.type?.value)
        setValue(Fields.Email.Label, data.label)
        setValue(Fields.Email.Address, data.address)
    }
}