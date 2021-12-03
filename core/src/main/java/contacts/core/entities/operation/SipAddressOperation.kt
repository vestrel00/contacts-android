package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.SipAddressField
import contacts.core.entities.MimeType
import contacts.core.entities.SipAddressEntity

internal class SipAddressOperation(isProfile: Boolean, includeFields: Set<SipAddressField>) :
    AbstractDataOperation<SipAddressField, SipAddressEntity>(isProfile, includeFields) {

    override val mimeType = MimeType.SipAddress

    override fun setValuesFromData(
        data: SipAddressEntity, setValue: (field: SipAddressField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.SipAddress.SipAddress, data.sipAddress)
    }
}