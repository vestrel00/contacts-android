package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.RelationEntity.Type
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a relation.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 *
 * Local RawContacts (those that are not associated with an Account) **should not** have any entries
 * of this data kind.
 */
sealed interface RelationEntity : DataEntity {

    /**
     * The [Type] of relation.
     */
    val type: Type?

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?

    /**
     * The name of the relative as the user entered it.
     */
    val name: String?

    override val mimeType: MimeType
        get() = MimeType.Relation

    // type and label are intentionally excluded as per documentation
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(name)

    enum class Type(override val value: Int) : DataEntity.Type {

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
 * An immutable [RelationEntity].
 */
@Parcelize
data class Relation internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val type: Type?,
    override val label: String?,

    override val name: String?

) : RelationEntity, ImmutableDataEntityWithMutableType<MutableRelation> {

    override fun mutableCopy() = MutableRelation(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label,

        name = name
    )
}

/**
 * A mutable [RelationEntity].
 */
@Parcelize
data class MutableRelation internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override var isPrimary: Boolean,
    override var isSuperPrimary: Boolean,

    override var type: Type?,
    override var label: String?,

    override var name: String?

) : RelationEntity, MutableDataEntityWithTypeAndLabel<Type> {

    constructor() : this(
        null, null, null, false, false,
        null, null, null
    )

    @IgnoredOnParcel
    override var primaryValue: String? by this::name
}