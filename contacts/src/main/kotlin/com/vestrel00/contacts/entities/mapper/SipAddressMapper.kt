package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.SipAddress
import com.vestrel00.contacts.entities.cursor.SipAddressCursor

internal class SipAddressMapper(private val sipAddressCursor: SipAddressCursor) :
    EntityMapper<SipAddress> {

    override val value: SipAddress
        get() = SipAddress(
            id = sipAddressCursor.dataId,
            rawContactId = sipAddressCursor.rawContactId,
            contactId = sipAddressCursor.contactId,

            isPrimary = sipAddressCursor.isPrimary,
            isSuperPrimary = sipAddressCursor.isSuperPrimary,

            sipAddress = sipAddressCursor.sipAddress
        )
}
