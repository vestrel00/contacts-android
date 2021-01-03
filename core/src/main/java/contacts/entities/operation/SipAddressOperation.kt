package contacts.entities.operation

import contacts.Fields
import contacts.SipAddressField
import contacts.entities.MimeType
import contacts.entities.MutableSipAddress

internal class SipAddressOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<SipAddressField, MutableSipAddress>(isProfile) {

    override val mimeType = MimeType.SipAddress

    override fun setData(
        data: MutableSipAddress, setValue: (field: SipAddressField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.SipAddress.SipAddress, data.sipAddress)
    }
}