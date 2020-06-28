package com.vestrel00.contacts.entities.operation

import com.vestrel00.contacts.Field
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableSipAddress

internal object SipAddressOperation : AbstractCommonDataOperation<MutableSipAddress>() {

    override val mimeType = MimeType.SIP_ADDRESS

    override fun setData(
        data: MutableSipAddress, setValue: (field: Field, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.SipAddress.SipAddress, data.sipAddress)
    }
}