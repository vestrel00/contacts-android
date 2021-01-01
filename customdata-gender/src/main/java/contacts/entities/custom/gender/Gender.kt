package contacts.entities.custom.gender

import contacts.entities.CommonDataEntity
import contacts.entities.MimeType
import contacts.entities.custom.CustomCommonDataEntity
import contacts.entities.custom.gender.Gender.Type
import kotlinx.android.parcel.Parcelize

/**
 * Describes the gender of a RawContact.
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

) : CustomCommonDataEntity {

    override val mimeType: MimeType.Custom = GenderMimeType

    /*
     * Typically, we do not consider type and label when determining if a piece of data is blank or
     * not. For example, phones have type, label, and number (the underlying primary data). Only the
     * number is important in that case. However, the primary data we are interested in here is the
     * actual type itself.
     */
    override val isBlank: Boolean = type == null

    enum class Type(override val value: Int) : CommonDataEntity.Type {

        MALE(1),
        FEMALE(2),

        // Does not really matter but custom is typically 0
        // See ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM
        CUSTOM(0);

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}
