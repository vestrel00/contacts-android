package contacts.core.entities

// Note that this is declared here and not in the entities.custom package because DataEntity is sealed here.
// If Kotlin increases the scope of "sealed" from package to module, then we can move it back =)

/**
 * A custom [DataEntity].
 *
 * Implementations are required to be parcelable. Kotlin users are recommended to use data class
 * combined with [kotlinx.parcelize.Parcelize].
 */
sealed interface CustomDataEntity : DataEntity {

    // Override this to cast type from MimeType to MimeType.Custom
    override val mimeType: MimeType.Custom
}

/**
 * An immutable [CustomDataEntity].
 */
interface ImmutableCustomDataEntity : CustomDataEntity, ImmutableDataEntity

/**
 * An immutable [CustomDataEntity].
 */
interface ImmutableCustomDataEntityWithMutableType<T : MutableCustomDataEntity> :
    ImmutableCustomDataEntity, ImmutableDataEntityWithMutableType<T>

/**
 * A mutable [CustomDataEntity].
 */
interface MutableCustomDataEntity : CustomDataEntity, MutableDataEntity

/**
 * A [MutableCustomDataEntity], with a mutable [type] and [label].
 */
interface MutableCustomDataWithTypeAndLabel<T : DataEntity.Type> : MutableCustomDataEntity,
    MutableDataEntityWithTypeAndLabel<T>