package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.RelationEntity.Type
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a relation.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 *
 * Local RawContacts (those that are not associated with an Account) **should not** have any entries
 * of this data kind.
 */
sealed interface RelationEntity : DataEntityWithTypeAndLabel<Type> {

    /**
     * The name of the relative as the user entered it.
     */
    val name: String?

    /**
     * The [name].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::name
    override val primaryValue: String?
        get() = name

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

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from RelationEntity, there is only one interface that extends it; MutableRelationEntity.
 *
 * The MutableRelationEntity interface is used for library constructs that require an RelationEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableRelation and NewRelation. With this, we can create constructs that can
 * keep a reference to MutableRelation(s) or NewRelation(s) through the MutableRelationEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewRelationEntity, ExistingRelationEntity, and
 * ImmutableRelationEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [RelationEntity]. `
 */
sealed interface MutableRelationEntity : RelationEntity, MutableDataEntityWithTypeAndLabel<Type> {

    override var name: String?

    /**
     * The [name].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::name
    override var primaryValue: String?
        get() = name
        set(value) {
            name = value
        }
}

/**
 * An existing immutable [RelationEntity].
 */
@Parcelize
data class Relation internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val type: Type?,
    override val label: String?,

    override val name: String?

) : RelationEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutableRelation> {

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
 * An existing mutable [RelationEntity].
 */
@Parcelize
data class MutableRelation internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var type: Type?,
    override var label: String?,

    override var name: String?

) : RelationEntity, ExistingDataEntity, MutableRelationEntity

/**
 * A new mutable [RelationEntity].
 */
@Parcelize
data class NewRelation @JvmOverloads constructor(

    override var type: Type? = null,
    override var label: String? = null,

    override var name: String? = null

) : RelationEntity, NewDataEntity, MutableRelationEntity