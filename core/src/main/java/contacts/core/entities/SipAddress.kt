package contacts.core.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a SIP address for the contact.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 */
sealed interface SipAddressEntity : DataEntity {

    // Type and Label are also available. However, it is unnecessary as there is only one sip
    // address per contact.

    /**
     * The SIP address.
     */
    val sipAddress: String?

    override val mimeType: MimeType
        get() = MimeType.SipAddress

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(sipAddress)
}

/**
 * An immutable [SipAddressEntity].
 */
@Parcelize
data class SipAddress internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val sipAddress: String?

) : SipAddressEntity, ImmutableDataEntityWithMutableType<MutableSipAddress> {

    override fun mutableCopy() = MutableSipAddress(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        sipAddress = sipAddress
    )
}

/**
 * A mutable [SipAddressEntity].
 */
@Parcelize
data class MutableSipAddress internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override var isPrimary: Boolean,
    override var isSuperPrimary: Boolean,

    override var sipAddress: String?

) : SipAddressEntity, MutableDataEntity {

    constructor() : this(null, null, null, false, false, null)

    @IgnoredOnParcel
    override var primaryValue: String? by this::sipAddress
}