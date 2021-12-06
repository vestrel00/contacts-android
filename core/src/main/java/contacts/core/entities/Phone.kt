package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.PhoneEntity.Type
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a telephone number.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
sealed interface PhoneEntity : DataEntity {

    /**
     * The [Type] of phone.
     *
     * Use [Type.labelStr] to get the display name of the type.
     */
    val type: Type?

    /**
     * Used as the string representation of the [type] if this is not null and the [type] is
     * [Type.CUSTOM]. Otherwise, the system's string representation of the type is used.
     *
     * This is the string value displayed in the UI for user-created custom types. This is only used
     * when the [type] is [Type.CUSTOM].
     */
    val label: String?

    /**
     * The phone number as the user entered it.
     *
     * E.G. (012) 345-6789
     */
    val number: String?

    /**
     * The phone number's E164 representation. This value can be omitted in which case the provider
     * will try to automatically infer it.  (It'll be left null if the provider fails to infer.)
     *
     * If present, [number] has to be set as well (it will be ignored otherwise).
     *
     * E.G. +10123456789
     */
    val normalizedNumber: String?

    override val mimeType: MimeType
        get() = MimeType.Phone

    // type and label are intentionally excluded as per documentation
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(number, normalizedNumber)

    enum class Type(override val value: Int) : DataEntity.Type {

        // Order of declaration is the same as seen in the native contacts app
        MOBILE(CommonDataKinds.Phone.TYPE_MOBILE), // Default
        WORK(CommonDataKinds.Phone.TYPE_WORK),
        HOME(CommonDataKinds.Phone.TYPE_HOME),
        MAIN(CommonDataKinds.Phone.TYPE_MAIN),
        FAX_WORK(CommonDataKinds.Phone.TYPE_FAX_WORK),
        FAX_HOME(CommonDataKinds.Phone.TYPE_FAX_HOME),
        PAGER(CommonDataKinds.Phone.TYPE_PAGER),
        OTHER(CommonDataKinds.Phone.TYPE_OTHER),
        CUSTOM(CommonDataKinds.Phone.TYPE_CUSTOM);

        /*
        Not including the rest of these because they are not shown in the native contacts app.
        Probably because they aren't useful? Or is there another reason? Maybe these should be
        visible too? Community will speak up if they want to on this matter.

        CALLBACK(CommonDataKinds.Phone.TYPE_CALLBACK),
        CAR(CommonDataKinds.Phone.TYPE_CAR),
        COMPANY_MAIN(CommonDataKinds.Phone.TYPE_COMPANY_MAIN),
        ISDN(CommonDataKinds.Phone.TYPE_ISDN),
        OTHER_FAX(CommonDataKinds.Phone.TYPE_OTHER_FAX),
        RADIO(CommonDataKinds.Phone.TYPE_RADIO),
        TELEX(CommonDataKinds.Phone.TYPE_TELEX),
        TTY_TDD(CommonDataKinds.Phone.TYPE_TTY_TDD),
        WORK_MOBILE(CommonDataKinds.Phone.TYPE_WORK_MOBILE),
        WORK_PAGER(CommonDataKinds.Phone.TYPE_WORK_PAGER),
        ASSISTANT(CommonDataKinds.Phone.TYPE_ASSISTANT),
        MMS(CommonDataKinds.Phone.TYPE_MMS),
         */

        override fun labelStr(resources: Resources, label: String?): String =
            CommonDataKinds.Phone.getTypeLabel(resources, value, label).toString()

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

/**
 * An immutable [PhoneEntity].
 */
@Parcelize
data class Phone internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val type: Type?,
    override val label: String?,

    override val number: String?,
    override val normalizedNumber: String?

) : PhoneEntity, ImmutableDataEntityWithMutableType<MutablePhone> {

    override fun mutableCopy() = MutablePhone(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label,

        number = number,
        normalizedNumber = normalizedNumber
    )


}

/**
 * A mutable [PhoneEntity].
 */
@Parcelize
data class MutablePhone internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var type: Type?,
    override var label: String?,

    override var number: String?,
    override var normalizedNumber: String?

) : PhoneEntity, MutableDataEntityWithTypeAndLabel<Type> {

    constructor() : this(
        null, null, null, false, false,
        null, null, null, null
    )

    @IgnoredOnParcel
    override var primaryValue: String? by this::number
}