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
// Intentionally not sealed so that consumers can define their own implementations.
interface ImmutableCustomDataEntity : CustomDataEntity, ImmutableDataEntity

/**
 * An [ImmutableCustomDataEntity] with a mutable type.
 */
// Intentionally not sealed so that consumers can define their own implementations.
interface ImmutableCustomDataEntityWithMutableType<T : MutableCustomDataEntity> :
    ImmutableCustomDataEntity, ImmutableDataEntityWithMutableType<T>

/**
 * An [ImmutableCustomDataEntity] that has a mutable type [T] that may or may not be null.
 */
// Intentionally not sealed so that consumers can define their own implementations.
sealed interface ImmutableCustomDataEntityWithNullableMutableType<T : MutableCustomDataEntity> :
    ImmutableCustomDataEntity, ImmutableDataEntityWithNullableMutableType<T>

/**
 * A mutable [CustomDataEntity].
 */
// Intentionally not sealed so that consumers can define their own implementations.
interface MutableCustomDataEntity : CustomDataEntity, MutableDataEntity

/**
 * A [MutableCustomDataEntity], with a mutable [type] and [label].
 */
// Intentionally not sealed so that consumers can define their own implementations.
interface MutableCustomDataWithTypeAndLabel<T : DataEntity.Type> : MutableCustomDataEntity,
    MutableDataEntityWithTypeAndLabel<T>