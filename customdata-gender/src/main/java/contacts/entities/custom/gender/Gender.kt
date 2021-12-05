package contacts.entities.custom.gender

import android.content.res.Resources
import android.provider.ContactsContract
import contacts.core.entities.*
import contacts.entities.custom.gender.GenderEntity.Type
import contacts.entities.custom.gender.GenderEntity.Type.*
import kotlinx.parcelize.Parcelize

/**
 * Describes the gender of a RawContact.
 *
 * A RawContact may only have one Gender entry.
 */
sealed interface GenderEntity : CustomDataEntity {

    /**
     * The [Type] of gender.
     */
    val type: Type?

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?

    override val mimeType: MimeType.Custom
        get() = GenderMimeType

    // The type is typically not a part of this check but it is the primary data in this case
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(type)

    /**
     * The types of gender. There are two main genders; [MALE] and [FEMALE].
     *
     * For other types of genders, use [CUSTOM] and [Gender.label]
     */
    enum class Type(override val value: Int) : DataEntity.Type {

        MALE(1),
        FEMALE(2),
        CUSTOM(ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM);

        override fun labelStr(resources: Resources, label: String?): String =
            if (this == CUSTOM && label?.isNotEmpty() == true) {
                label
            } else {
                resources.getText(typeLabelResource).toString()
            }

        private val typeLabelResource: Int
            get() = when (this) {
                MALE -> R.string.customdata_gender_male
                FEMALE -> R.string.customdata_gender_female
                CUSTOM -> R.string.customdata_gender_custom
            }

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

/**
 * An immutable [GenderEntity].
 */
@Parcelize
data class Gender internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val type: Type?,
    override val label: String?

) : GenderEntity, ImmutableCustomDataEntityWithMutableType<MutableGender> {

    override fun mutableCopy() = MutableGender(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label
    )
}

/**
 * A mutable [GenderEntity].
 */
@Parcelize
data class MutableGender internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var type: Type?,
    override var label: String?

) : GenderEntity, MutableCustomDataEntityWithTypeAndLabel<Type> {

    constructor() : this(null, null, null, false, false, null, null)

    // The primary value is type (and label if custom). So, this does nothing to avoid complicating
    // the API implementation.
    override var primaryValue: String?
        get() = null
        set(_) {}
}