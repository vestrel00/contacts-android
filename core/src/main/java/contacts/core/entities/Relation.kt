package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.Relation.Type
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a relation.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 *
 * Local RawContacts (those that are not associated with an Account) **should not** have any entries
 * of this data kind.
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class Relation internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The [Type] of relation.
     */
    val type: Type?,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?,

    /**
     * The name of the relative as the user entered it.
     */
    val name: String?

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Relation

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(type, label, name)

    fun toMutableRelation() = MutableRelation(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label,

        name = name
    )

    enum class Type(override val value: Int) : CommonDataEntity.Type {

        // Order of declaration is the same as seen in the native contacts app
        ASSISTANT(CommonDataKinds.Relation.TYPE_ASSISTANT), // Default
        BROTHER(CommonDataKinds.Relation.TYPE_BROTHER),
        CHILD(CommonDataKinds.Relation.TYPE_CHILD),
        DOMESTIC_PARTNER(CommonDataKinds.Relation.TYPE_DOMESTIC_PARTNER),
        FATHER(CommonDataKinds.Relation.TYPE_FATHER),
        FRIEND(CommonDataKinds.Relation.TYPE_FRIEND),
        MANAGER(CommonDataKinds.Relation.TYPE_MANAGER),
        MOTHER(CommonDataKinds.Relation.TYPE_MOTHER),
        PARENT(CommonDataKinds.Relation.TYPE_PARENT),
        PARTNER(CommonDataKinds.Relation.TYPE_PARTNER),
        REFERRED_BY(CommonDataKinds.Relation.TYPE_REFERRED_BY),
        RELATIVE(CommonDataKinds.Relation.TYPE_RELATIVE),
        SISTER(CommonDataKinds.Relation.TYPE_SISTER),
        SPOUSE(CommonDataKinds.Relation.TYPE_SPOUSE),
        CUSTOM(CommonDataKinds.Relation.TYPE_CUSTOM);

        override fun labelStr(resources: Resources, label: String?): String =
            CommonDataKinds.Relation.getTypeLabel(resources, value, label).toString()

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

/**
 * A mutable [Relation].
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class MutableRelation internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Relation.type].
     */
    override var type: Type?,

    /**
     * See [Relation.label].
     */
    override var label: String?,

    /**
     * See [Relation.name].
     */
    var name: String?

) : MutableCommonDataEntityWithType<Type> {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Relation

    constructor() : this(
        null, null, null, false, false,
        null, null, null
    )

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(type, label, name)

    @IgnoredOnParcel
    override var primaryValue: String? by this::name
}