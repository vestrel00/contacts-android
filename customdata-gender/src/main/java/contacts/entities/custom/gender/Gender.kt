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
 * A RawContact may have 0 or 1 entry of this data kind.
 */
sealed interface GenderEntity : CustomDataEntityWithTypeAndLabel<Type> {

    // The primary value is type (and label if custom). So, this does nothing to avoid complicating
    // the API implementation. Therefore, it is unused and will always return null.
    override val primaryValue: String?
        get() = null

    override val mimeType: MimeType.Custom
        get() = GenderMimeType

    // The type is typically not a part of this check but it is the primary data in this case
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(type)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): GenderEntity

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

            fun fromValue(value: Int?): Type? = entries.find { it.value == value }
        }
    }
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from GenderEntity, there is only one interface that extends it; MutableGenderEntity.
 *
 * The MutableGenderEntity interface is used for library constructs that require an GenderEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableGender and NewGender. With this, we can create constructs that can
 * keep a reference to MutableGender(s) or NewGender(s) through the MutableGenderEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewGenderEntity, ExistingGenderEntity, and
 * ImmutableGenderEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [GenderEntity]. `
 */
sealed interface MutableGenderEntity : GenderEntity, MutableCustomDataEntityWithTypeAndLabel<Type> {

    // The primary value is type (and label if custom). So, this does nothing to avoid complicating
    // the API implementation. Therefore, it is unused and will always return null.
    override var primaryValue: String?
        get() = null
        set(_) {}

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableGenderEntity
}

/**
 * An existing immutable [GenderEntity].
 */
@ConsistentCopyVisibility
@Parcelize
data class Gender internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val type: Type?,
    override val label: String?,

    override val isRedacted: Boolean

) : GenderEntity, ExistingCustomDataEntity,
    ImmutableCustomDataEntityWithMutableType<MutableGender> {

    override fun mutableCopy() = MutableGender(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label,

        isRedacted = isRedacted
    )

    // Nothing to redact.
    override fun redactedCopy() = copy(isRedacted = true)
}

/**
 * An existing mutable [GenderEntity].
 */
@ConsistentCopyVisibility
@Parcelize
data class MutableGender internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var type: Type?,
    override var label: String?,

    override val isRedacted: Boolean

) : GenderEntity, ExistingCustomDataEntity, MutableGenderEntity {

    // Nothing to redact.
    override fun redactedCopy() = copy(isRedacted = true)
}

/**
 * A new mutable [GenderEntity].
 */
@Parcelize
data class NewGender @JvmOverloads constructor(

    override var type: Type? = null,
    override var label: String? = null,

    override var isReadOnly: Boolean = false,
    override val isRedacted: Boolean = false

) : GenderEntity, NewCustomDataEntity, MutableGenderEntity {

    // Nothing to redact.
    override fun redactedCopy() = copy(isRedacted = true)
}