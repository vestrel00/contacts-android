package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.ImEntity.Protocol
import kotlinx.parcelize.IgnoredOnParcel
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

/**
 * An immutable [ImEntity].
 */
@Parcelize
data class Im internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val protocol: Protocol?,
    override val customProtocol: String?,
    override val data: String?

) : ImEntity, ImmutableDataEntityWithMutableType<MutableIm> {

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
 * A mutable [ImEntity].
 */
@Parcelize
data class MutableIm internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override var isPrimary: Boolean,
    override var isSuperPrimary: Boolean,

    override var protocol: Protocol?,
    override var customProtocol: String?,
    override var data: String?

) : ImEntity, MutableDataEntityWithTypeAndLabel<Protocol> {

    constructor() : this(
        null, null, null, false, false,
        null, null, null
    )

    @IgnoredOnParcel
    override var primaryValue: String? by this::data

    @IgnoredOnParcel
    override var type: Protocol? by this::protocol

    @IgnoredOnParcel
    override var label: String? by this::customProtocol
}