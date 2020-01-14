package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableSipAddress
import com.vestrel00.contacts.entities.cursor.SipAddressCursor

internal class SipAddressMapper(private val sipAddressCursor: SipAddressCursor) {

    val sipAddress: MutableSipAddress
        get() = MutableSipAddress(
            id = sipAddressCursor.id,
            rawContactId = sipAddressCursor.rawContactId,
            contactId = sipAddressCursor.contactId,

            isPrimary = sipAddressCursor.isPrimary,
            isSuperPrimary = sipAddressCursor.isSuperPrimary,

            sipAddress = sipAddressCursor.sipAddress
        )
}
