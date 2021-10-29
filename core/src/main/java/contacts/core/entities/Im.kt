package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.Im.Protocol
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing an instant messaging address.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class Im internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    // Type and Label are also available. However, they have no use here as the protocol and custom
    // protocol have taken their place...

    /**
     * The [Protocol] of this Im.
     */
    val protocol: Protocol?,

    /**
     * The name of the custom protocol. Used when the [protocol] is [Protocol.CUSTOM].
     */
    val customProtocol: String?,

    /**
     * The data as the user entered it.
     */
    val data: String?

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Im

    // protocol (type) and customProtocol (label) are intentionally excluded as per documentation
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(data)

    fun toMutableIm() = MutableIm(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        protocol = protocol,
        customProtocol = customProtocol,

        data = data
    )

    enum class Protocol(override val value: Int) : CommonDataEntity.Type {

        // Type is also defined within CmmonDataKinds.Im... Ignore those. Type (and label) may have
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
 * A mutable [Im].
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class MutableIm internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Im.protocol].
     */
    var protocol: Protocol?,

    /**
     * See [Im.customProtocol].
     */
    var customProtocol: String?,

    /**
     * See [Im.data].
     */
    var data: String?

) : MutableCommonDataEntityWithType<Protocol> {

    constructor() : this(
        null, null, null, false, false,
        null, null, null
    )

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Im

    // protocol (type) and customProtocol (label) are intentionally excluded as per documentation
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(data)

    @IgnoredOnParcel
    override var primaryValue: String? by this::data

    override var type: Protocol?
        get() = protocol
        set(value) {
            protocol = value
        }

    override var label: String?
        get() = customProtocol
        set(value) {
            customProtocol = value
        }
}