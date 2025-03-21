@file:Suppress("Deprecation")

package contacts.core.entities

import contacts.core.DEPRECATED_SIP_ADDRESS
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a SIP address for the contact.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 */
@Deprecated(DEPRECATED_SIP_ADDRESS)
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

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): SipAddressEntity
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
@Deprecated(DEPRECATED_SIP_ADDRESS)
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

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableSipAddressEntity
}

/**
 * An existing immutable [SipAddressEntity].
 */
@Deprecated(DEPRECATED_SIP_ADDRESS)
@ConsistentCopyVisibility
@Parcelize
data class SipAddress internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val sipAddress: String?,

    override val isRedacted: Boolean

) : SipAddressEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutableSipAddress> {

    override fun mutableCopy() = MutableSipAddress(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        sipAddress = sipAddress,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        sipAddress = sipAddress?.redact()
    )
}

/**
 * An existing mutable [SipAddressEntity].
 */
@Deprecated(DEPRECATED_SIP_ADDRESS)
@ConsistentCopyVisibility
@Parcelize
data class MutableSipAddress internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var sipAddress: String?,

    override val isRedacted: Boolean

) : SipAddressEntity, ExistingDataEntity, MutableSipAddressEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        sipAddress = sipAddress?.redact()
    )
}

/**
 * A new mutable [SipAddressEntity].
 */
@Deprecated(DEPRECATED_SIP_ADDRESS)
@Parcelize
data class NewSipAddress @JvmOverloads constructor(

    override var sipAddress: String? = null,

    override var isReadOnly: Boolean = false,
    override val isRedacted: Boolean = false

) : SipAddressEntity, NewDataEntity, MutableSipAddressEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        sipAddress = sipAddress?.redact()
    )
}