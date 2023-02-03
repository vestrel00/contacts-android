package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.PhoneEntity.Type
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a telephone number.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 */
sealed interface PhoneEntity : DataEntityWithTypeAndLabel<Type> {

    /**
     * The phone number to block as the user entered it.
     *
     * This may or may not be formatted (e.g. (012) 345-6789).
     */
    val number: String?

    /**
     * The [number]'s E164 representation. This value can be omitted in which case the provider
     * will try to automatically infer it. (It'll be left null if the provider fails to infer.)
     *
     * If present, [number] has to be set as well (it will be ignored otherwise).
     *
     * E.G. +10123456789
     *
     * If you want to set this value yourself, you may want to look at
     * [android.telephony.PhoneNumberUtils].
     */
    val normalizedNumber: String?

    /**
     * The [number].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::number
    override val primaryValue: String?
        get() = number

    override val mimeType: MimeType
        get() = MimeType.Phone

    // type and label are intentionally excluded as per documentation
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(number, normalizedNumber)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): PhoneEntity

    enum class Type(override val value: Int) : DataEntity.Type {

        // Order of declaration is the same as seen in the AOSP contacts app
        MOBILE(CommonDataKinds.Phone.TYPE_MOBILE), // Default
        WORK(CommonDataKinds.Phone.TYPE_WORK),
        HOME(CommonDataKinds.Phone.TYPE_HOME),
        MAIN(CommonDataKinds.Phone.TYPE_MAIN),
        FAX_WORK(CommonDataKinds.Phone.TYPE_FAX_WORK),
        FAX_HOME(CommonDataKinds.Phone.TYPE_FAX_HOME),
        PAGER(CommonDataKinds.Phone.TYPE_PAGER),
        OTHER(CommonDataKinds.Phone.TYPE_OTHER),
        CUSTOM(CommonDataKinds.Phone.TYPE_CUSTOM),
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
        MMS(CommonDataKinds.Phone.TYPE_MMS);

        override fun labelStr(resources: Resources, label: String?): String =
            CommonDataKinds.Phone.getTypeLabel(resources, value, label).toString()

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from PhoneEntity, there is only one interface that extends it; MutablePhoneEntity.
 *
 * The MutablePhoneEntity interface is used for library constructs that require an PhoneEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutablePhone and NewPhone. With this, we can create constructs that can
 * keep a reference to MutablePhone(s) or NewPhone(s) through the MutablePhoneEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewPhoneEntity, ExistingPhoneEntity, and
 * ImmutablePhoneEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [PhoneEntity]. `
 */
sealed interface MutablePhoneEntity : PhoneEntity, MutableDataEntityWithTypeAndLabel<Type> {

    override var number: String?
    override var normalizedNumber: String?

    /**
     * The [number].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::number
    override var primaryValue: String?
        get() = number
        set(value) {
            number = value
        }

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutablePhoneEntity
}

/**
 * An existing immutable [PhoneEntity].
 */
@Parcelize
data class Phone internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val type: Type?,
    override val label: String?,

    override val number: String?,
    override val normalizedNumber: String?,

    override val isRedacted: Boolean

) : PhoneEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutablePhone> {

    override fun mutableCopy() = MutablePhone(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label,

        number = number,
        normalizedNumber = normalizedNumber,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        number = number?.redact(),
        normalizedNumber = normalizedNumber?.redact()
    )
}

/**
 * An existing mutable [PhoneEntity].
 */
@Parcelize
data class MutablePhone internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var type: Type?,
    override var label: String?,

    override var number: String?,
    override var normalizedNumber: String?,

    override val isRedacted: Boolean

) : PhoneEntity, ExistingDataEntity, MutablePhoneEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        number = number?.redact(),
        normalizedNumber = normalizedNumber?.redact()
    )
}

/**
 * A new mutable [PhoneEntity].
 */
@Parcelize
data class NewPhone @JvmOverloads constructor(

    override var type: Type? = null,
    override var label: String? = null,

    override var number: String? = null,
    override var normalizedNumber: String? = null,

    override val isRedacted: Boolean = false

) : PhoneEntity, NewDataEntity, MutablePhoneEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        number = number?.redact(),
        normalizedNumber = normalizedNumber?.redact()
    )
}