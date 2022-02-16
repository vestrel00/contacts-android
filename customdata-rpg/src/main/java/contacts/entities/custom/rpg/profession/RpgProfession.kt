package contacts.entities.custom.rpg.profession

import contacts.core.entities.*
import contacts.entities.custom.rpg.RpgMimeType
import kotlinx.parcelize.Parcelize

/**
 * The RPG profession of a RawContact.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 */
sealed interface RpgProfessionEntity : CustomDataEntity {

    /**
     * The profession title.
     *
     * E.G. "swordsman", "magician", etc.
     */
    val title: String?

    /**
     * The [title].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::title
    override val primaryValue: String?
        get() = title

    override val mimeType: MimeType.Custom
        get() = RpgMimeType.Profession

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(title)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): RpgProfessionEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from RpgProfessionEntity, there is only one interface that extends it;
 * MutableRpgProfessionEntity.
 *
 * The MutableRpgProfessionEntity interface is used for library constructs that require an
 * RpgProfessionEntity that can be mutated whether it is already inserted in the database or not.
 * There are two variants of this; MutableRpgProfession and NewRpgProfession. With this, we can
 * create constructs that can keep a reference to MutableRpgProfession(s) or NewRpgProfession(s)
 * through the MutableRpgProfessionEntity abstraction/facade.
 *
 * This is why there are no interfaces for NewRpgProfessionEntity, ExistingRpgProfessionEntity, and
 * ImmutableRpgProfessionEntity. There are currently no library functions or constructs that
 * require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [RpgProfessionEntity]. `
 */
sealed interface MutableRpgProfessionEntity : RpgProfessionEntity, MutableCustomDataEntity {

    override var title: String?

    /**
     * The [title].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::title
    override var primaryValue: String?
        get() = title
        set(value) {
            title = value
        }

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableRpgProfessionEntity
}

/**
 * An existing immutable [RpgProfessionEntity].
 */
@Parcelize
data class RpgProfession internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val title: String?,

    override val isRedacted: Boolean

) : RpgProfessionEntity, ExistingCustomDataEntity,
    ImmutableCustomDataEntityWithMutableType<MutableRpgProfession> {

    override fun mutableCopy() = MutableRpgProfession(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        title = title,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        title = title?.redact()
    )
}

/**
 * An existing mutable [RpgProfessionEntity].
 */
@Parcelize
data class MutableRpgProfession internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var title: String?,

    override val isRedacted: Boolean

) : RpgProfessionEntity, ExistingCustomDataEntity, MutableRpgProfessionEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        title = title?.redact()
    )
}

/**
 * A new mutable [RpgProfessionEntity].
 */
@Parcelize
data class NewRpgProfession @JvmOverloads constructor(

    override var title: String? = null,

    override val isRedacted: Boolean = false

) : RpgProfessionEntity, NewCustomDataEntity, MutableRpgProfessionEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        title = title?.redact()
    )
}
