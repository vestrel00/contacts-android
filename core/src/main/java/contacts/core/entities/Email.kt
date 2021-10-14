package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.Email.Type
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing an email address.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class Email internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The [Type] of email.
     */
    val type: Type?,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?,

    /**
     * The email address.
     */
    val address: String?

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Email

    // type and label are excluded from this check as they are useless information by themselves
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(address)

    fun toMutableEmail() = MutableEmail(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label,

        address = address
    )

    enum class Type(override val value: Int) : CommonDataEntity.Type {

        // Order of declaration is the same as seen in the native contacts app
        HOME(CommonDataKinds.Email.TYPE_HOME), // Default
        WORK(CommonDataKinds.Email.TYPE_WORK),
        OTHER(CommonDataKinds.Email.TYPE_OTHER),
        CUSTOM(CommonDataKinds.Email.TYPE_CUSTOM);

        // For some reason, the native contacts app does not show this type in the UI. Thus, we will
        // do the same =)
        // MOBILE(CommonDataKinds.Email.TYPE_MOBILE)

        override fun labelStr(resources: Resources, label: String?): String =
            CommonDataKinds.Email.getTypeLabel(resources, value, label).toString()

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

/**
 * A mutable [Email].
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class MutableEmail internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Email.type].
     */
    override var type: Type?,

    /**
     * See [Email.label].
     */
    override var label: String?,

    /**
     * See [Email.address].
     */
    var address: String?

) : MutableCommonDataEntityWithType<Type> {

    constructor() : this(
        null, null, null, false, false,
        null, null, null
    )

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Email

    // type and label are excluded from this check as they are useless information by themselves
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(address)

    @IgnoredOnParcel
    override var primaryValue: String? by this::address
}