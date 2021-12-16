package contacts.core.entities

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

    /**
     * The [sipAddress].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::sipAddress
    override val primaryValue: String?
        get() = sipAddress

    override val mimeType: MimeType
        get() = MimeType.SipAddress

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(sipAddress)
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from SipAddressEntity, there is only one interface that extends it; MutableSipAddressEntity.
 *
 * The MutableSipAddressEntity interface is used for library constructs that require an SipAddressEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableSipAddress and NewSipAddress. With this, we can create constructs that can
 * keep a reference to MutableSipAddress(s) or NewSipAddress(s) through the MutableSipAddressEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewSipAddressEntity, ExistingSipAddressEntity, and
 * ImmutableSipAddressEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [SipAddressEntity]. `
 */
sealed interface MutableSipAddressEntity : SipAddressEntity, MutableDataEntity {

    override var sipAddress: String?

    /**
     * The [sipAddress].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::sipAddress
    override var primaryValue: String?
        get() = sipAddress
        set(value) {
            sipAddress = value
        }
}

/**
 * An existing immutable [SipAddressEntity].
 */
@Parcelize
data class SipAddress internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val sipAddress: String?

) : SipAddressEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutableSipAddress> {

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
 * An existing mutable [SipAddressEntity].
 */
@Parcelize
data class MutableSipAddress internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var sipAddress: String?

) : SipAddressEntity, ExistingDataEntity, MutableSipAddressEntity

/**
 * A new mutable [SipAddressEntity].
 */
@Parcelize
data class NewSipAddress @JvmOverloads constructor(

    override var sipAddress: String? = null

) : SipAddressEntity, NewDataEntity, MutableSipAddressEntity