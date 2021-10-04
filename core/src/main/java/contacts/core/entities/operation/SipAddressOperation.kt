package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.SipAddressField
import contacts.core.entities.MimeType
import contacts.core.entities.MutableSipAddress

internal class SipAddressOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<SipAddressField, MutableSipAddress>(isProfile) {

    override val mimeType = MimeType.SipAddress

    override fun setData(
        data: MutableSipAddress, setValue: (field: SipAddressField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.SipAddress.SipAddress, data.sipAddress)
    }
}