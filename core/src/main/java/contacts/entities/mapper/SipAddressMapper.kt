package contacts.entities.mapper

import contacts.entities.SipAddress
import contacts.entities.cursor.SipAddressCursor

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
