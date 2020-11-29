package contacts.entities.operation

import contacts.Field
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableSipAddress

internal class SipAddressOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableSipAddress>(isProfile) {

    override val mimeType = MimeType.SipAddress

    override fun setData(
        data: MutableSipAddress, setValue: (field: Field, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.SipAddress.SipAddress, data.sipAddress)
    }
}