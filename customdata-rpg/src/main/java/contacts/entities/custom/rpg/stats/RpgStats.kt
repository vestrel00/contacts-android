package contacts.entities.custom.rpg.stats

import contacts.core.entities.*
import contacts.entities.custom.rpg.RpgMimeType
import kotlinx.parcelize.Parcelize

/**
 * The RPG stats of a RawContact.
 *
 * More info in http://howtomakeanrpg.com/a/how-to-make-an-rpg-stats.html
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 */
sealed interface RpgStatsEntity : CustomDataEntity {

    val level: Int?
    val speed: Int?
    val strength: Int?
    val intelligence: Int?
    val luck: Int?

    /**
     * The [level].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::level
    override val primaryValue: String?
        get() = level?.toString()

    override val mimeType: MimeType.Custom
        get() = RpgMimeType.Stats

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(level, speed, strength, intelligence, luck)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): RpgStatsEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from RpgStatsEntity, there is only one interface that extends it; MutableRpgStatsEntity.
 *
 * The MutableRpgStatsEntity interface is used for library constructs that require an RpgStatsEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableRpgStats and NewRpgStats. With this, we can create constructs that can
 * keep a reference to MutableRpgStats(s) or NewRpgStats(s) through the MutableRpgStatsEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewRpgStatsEntity, ExistingRpgStatsEntity, and
 * ImmutableRpgStatsEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [RpgStatsEntity]. `
 */
sealed interface MutableRpgStatsEntity : RpgStatsEntity, MutableCustomDataEntity {

    override var level: Int?
    override var speed: Int?
    override var strength: Int?
    override var intelligence: Int?
    override var luck: Int?

    /**
     * The [level].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::level
    override var primaryValue: String?
        get() = level?.toString()
        set(value) {
            level = value?.toInt()
        }

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableRpgStatsEntity
}

/**
 * An existing immutable [RpgStatsEntity].
 */
@Parcelize
data class RpgStats internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var level: Int?,
    override var speed: Int?,
    override var strength: Int?,
    override var intelligence: Int?,
    override var luck: Int?,

    override val isRedacted: Boolean

) : RpgStatsEntity, ExistingCustomDataEntity,
    ImmutableCustomDataEntityWithMutableType<MutableRpgStats> {

    override fun mutableCopy() = MutableRpgStats(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        level = level,
        speed = speed,
        strength = strength,
        intelligence = intelligence,
        luck = luck,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        level = level?.redact(),
        speed = speed?.redact(),
        strength = strength?.redact(),
        intelligence = intelligence?.redact(),
        luck = luck?.redact()
    )
}

/**
 * An existing mutable [RpgStatsEntity].
 */
@Parcelize
data class MutableRpgStats internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var level: Int?,
    override var speed: Int?,
    override var strength: Int?,
    override var intelligence: Int?,
    override var luck: Int?,

    override val isRedacted: Boolean

) : RpgStatsEntity, ExistingCustomDataEntity, MutableRpgStatsEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        level = level?.redact(),
        speed = speed?.redact(),
        strength = strength?.redact(),
        intelligence = intelligence?.redact(),
        luck = luck?.redact()
    )
}

/**
 * A new mutable [RpgStatsEntity].
 */
@Parcelize
data class NewRpgStats @JvmOverloads constructor(

    override var level: Int? = null,
    override var speed: Int? = null,
    override var strength: Int? = null,
    override var intelligence: Int? = null,
    override var luck: Int? = null,

    override val isRedacted: Boolean = false

) : RpgStatsEntity, NewCustomDataEntity, MutableRpgStatsEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        level = level?.redact(),
        speed = speed?.redact(),
        strength = strength?.redact(),
        intelligence = intelligence?.redact(),
        luck = luck?.redact()
    )
}
