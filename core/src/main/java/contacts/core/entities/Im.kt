package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.ImEntity.Protocol
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing an instant messaging address.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 */
sealed interface ImEntity : DataEntity {

    // Type and Label are also available. However, they have no use here as the protocol and custom
    // protocol have taken their place...

    /**
     * The [Protocol] of this Im.
     */
    val protocol: Protocol?

    /**
     * The name of the custom protocol. Used when the [protocol] is [Protocol.CUSTOM].
     */
    val customProtocol: String?

    /**
     * The data as the user entered it.
     */
    val data: String?

    override val mimeType: MimeType
        get() = MimeType.Im

    // protocol (type) and customProtocol (label) are intentionally excluded as per documentation
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(data)

    enum class Protocol(override val value: Int) : DataEntity.Type {

        // Type is also defined within CommonDataKinds.Im... Ignore those. Type (and label) may have
        // been deprecated by protocol and protocol label. Or what probably happened was that there
        // was one dev that did not want to use "type and label" for IM so he/she used "protocol"
        // \_-_-_/. One thing is for sure IMO. The dev who wrote this code is different from the dev
        // that wrote most of the CommonDataKinds.

        // Order of declaration is the same as seen in the native contacts app
        AIM(CommonDataKinds.Im.PROTOCOL_AIM), // Default
        MSN(CommonDataKinds.Im.PROTOCOL_MSN),
        YAHOO(CommonDataKinds.Im.PROTOCOL_YAHOO),
        SKYPE(CommonDataKinds.Im.PROTOCOL_SKYPE),
        QQ(CommonDataKinds.Im.PROTOCOL_QQ),
        HANGOUTS(CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK),
        ICQ(CommonDataKinds.Im.PROTOCOL_ICQ),
        JABBER(CommonDataKinds.Im.PROTOCOL_JABBER),
        CUSTOM(CommonDataKinds.Im.PROTOCOL_CUSTOM);

        // Not including the rest of these because they are not shown in the native contacts app.
        // Probably because they aren't useful? Or is there another reason? Maybe these should be
        // visible too? Community will speak up if they want to on this matter.
        // NET_MEETING(CommonDataKinds.Im.PROTOCOL_NETMEETING),

        // Unlike the other CommonDataKinds that use TYPE_CUSTOM (0), Im uses PROTOCOL_CUSTOM (-1).
        override val isCustomType: Boolean
            get() = value == CommonDataKinds.Im.PROTOCOL_CUSTOM

        override fun labelStr(resources: Resources, label: String?): String =
            // Make sure not to use getTypeLabel here!
            CommonDataKinds.Im.getProtocolLabel(resources, value, label).toString()

        internal companion object {

            fun fromValue(value: Int?): Protocol? = values().find { it.value == value }
        }
    }
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from ImEntity, there is only one interface that extends it; MutableImEntity.
 *
 * The MutableImEntity interface is used for library constructs that require an ImEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableIm and NewIm. With this, we can create constructs that can
 * keep a reference to MutableIm(s) or NewIm(s) through the MutableImEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewImEntity, ExistingImEntity, and
 * ImmutableImEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [ImEntity]. `
 */
sealed interface MutableImEntity : ImEntity, MutableDataEntityWithTypeAndLabel<Protocol> {

    override var protocol: Protocol?
    override var customProtocol: String?
    override var data: String?

    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::data
    override var primaryValue: String?
        get() = data
        set(value) {
            data = value
        }

    // Delegated properties are not allowed on interfaces =(
    // override var type: Protocol? by this::protocol
    override var type: Protocol?
        get() = protocol
        set(value) {
            protocol = value
        }

    // Delegated properties are not allowed on interfaces =(
    // override var label: String? by this::customProtocol
    override var label: String?
        get() = customProtocol
        set(value) {
            customProtocol = value
        }
}

/**
 * An existing immutable [ImEntity].
 */
@Parcelize
data class Im internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val protocol: Protocol?,
    override val customProtocol: String?,
    override val data: String?

) : ImEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutableIm> {

    override fun mutableCopy() = MutableIm(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        protocol = protocol,
        customProtocol = customProtocol,

        data = data
    )
}

/**
 * An existing mutable [ImEntity].
 */
@Parcelize
data class MutableIm internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var protocol: Protocol?,
    override var customProtocol: String?,
    override var data: String?

) : ImEntity, ExistingDataEntity, MutableImEntity

/**
 * A new mutable [ImEntity].
 */
// Intentionally expose primary constructor to consumers. Useful for Kotlin users.
@Parcelize
data class NewIm(

    override var protocol: Protocol?,
    override var customProtocol: String?,
    override var data: String?

) : ImEntity, NewDataEntity, MutableImEntity {

    // An empty constructor for consumer use. Useful for both Kotlin and Java users.
    constructor() : this(null, null, null)
}