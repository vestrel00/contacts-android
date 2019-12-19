package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SipAddress internal constructor(

    /**
     * The id of this row in the Data table.
     */
    override val id: Long,

    /**
     * The id of the [RawContact] this data belongs to.
     */
    override val rawContactId: Long,

    /**
     * The id of the [Contact] that this data entity is associated with.
     */
    override val contactId: Long,

    // Type and Label are also available. However, it is unnecessary as there is only one sip
    // address per contact.

    /**
     * The SIP address.
     */
    val sipAddress: String?

) : DataEntity, Parcelable {

    fun toMutableSipAddress() = MutableSipAddress(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        sipAddress = sipAddress
    )
}

@Parcelize
data class MutableSipAddress internal constructor(

    /**
     * See [SipAddress.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * See [SipAddress.rawContactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val rawContactId: Long,

    /**
     * See [SipAddress.contactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val contactId: Long,

    /**
     * See [SipAddress.sipAddress].
     */
    var sipAddress: String?

) : DataEntity, Parcelable {

    constructor() : this(INVALID_ID, INVALID_ID, INVALID_ID, null)

    internal fun toSipAddress() = SipAddress(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        sipAddress = sipAddress
    )
}