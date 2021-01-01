package contacts.custom.data.moreinfo

import contacts.custom.AbstractCustomCommonDataEntity
import contacts.custom.data.moreinfo.Gender.Type
import contacts.entities.CommonDataEntity
import contacts.entities.MimeType
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

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

) : AbstractCustomCommonDataEntity(id, rawContactId, contactId, isPrimary, isSuperPrimary) {

    override val mimeType: MimeType
        get() = TODO("Not yet implemented")

    /*
     * Typically, we do not consider type and label when determining if a piece of data is blank or
     * not. For example, phones have type, label, and number (the underlying primary data). Only the
     * number is important in that case. However, the primary data we are interested in here is the
     * actual type itself.
     */
    @IgnoredOnParcel
    override val isBlank: Boolean = type == null

    enum class Type(override val value: Int) : CommonDataEntity.Type {

        MALE(TYPE_MALE),
        FEMALE(TYPE_FEMALE),
        CUSTOM(TYPE_CUSTOM);

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

private const val TYPE_MALE = 1
private const val TYPE_FEMALE = 2

// Does not really matter but custom is typically 0
// See ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM
private const val TYPE_CUSTOM = 0