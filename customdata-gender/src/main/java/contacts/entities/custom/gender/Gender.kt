package contacts.entities.custom.gender

import android.content.res.Resources
import android.provider.ContactsContract
import contacts.core.entities.CommonDataEntity
import contacts.core.entities.MimeType
import contacts.core.entities.custom.CustomDataEntity
import contacts.core.entities.custom.MutableCustomDataEntityWithType
import contacts.core.entities.propertiesAreAllNullOrBlank
import contacts.entities.custom.gender.Gender.Type
import contacts.entities.custom.gender.Gender.Type.*
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Describes the gender of a RawContact.
 *
 * A RawContact may only have one Gender entry.
 */
@Parcelize
data class Gender internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The [Type] of gender.
     */
    val type: Type?,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?

) : CustomDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType.Custom = GenderMimeType

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(type, label)

    fun toMutableGender() = MutableGender(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label
    )

    /**
     * The types of gender. There are two main genders; [MALE] and [FEMALE].
     *
     * For other types of genders, use [CUSTOM] and [Gender.label]
     */
    enum class Type(override val value: Int) : CommonDataEntity.Type {

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
 * A mutable [Gender].
 */
@Parcelize
data class MutableGender internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * See [Gender.type].
     */
    override var type: Type?,

    /**
     * See [Gender.label].
     */
    override var label: String?

) : MutableCustomDataEntityWithType<Type> {

    constructor() : this(null, null, null, false, false, null, null)

    @IgnoredOnParcel
    override val mimeType: MimeType.Custom = GenderMimeType

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(type, label)

    internal fun toGender() = Gender(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label
    )

    // The primary value is type AND label (if custom). So, this does nothing to avoid complicating
    // the API implementation.
    override var primaryValue: String?
        get() = null
        set(_) {}
}