package contacts.core.entities

// Note that this is declared here and not in the entities.custom package because DataEntity is sealed here.
// If Kotlin increases the scope of "sealed" from package to module, then we can move it back =)

/**
 * A custom [DataEntity].
 *
 * Implementations are required to be parcelable. Kotlin users are recommended to use data class
 * combined with [kotlinx.parcelize.Parcelize].
 */
// Intentionally not sealed so that consumers can define their own implementations.
interface CustomDataEntity : DataEntity {

    // Override this to cast type from MimeType to MimeType.Custom
    override val mimeType: MimeType.Custom

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): CustomDataEntity
}

/**
 * A custom [DataEntityWithTypeAndLabel].
 *
 * Implementations are required to be parcelable. Kotlin users are recommended to use data class
 * combined with [kotlinx.parcelize.Parcelize].
 */
// Intentionally not sealed so that consumers can define their own implementations.
interface CustomDataEntityWithTypeAndLabel<T : DataEntity.Type> : CustomDataEntity,
    DataEntityWithTypeAndLabel<T> {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): CustomDataEntityWithTypeAndLabel<T>
}

/**
 * A [DataEntity] that has NOT yet been inserted into the database.
 */
interface NewCustomDataEntity : CustomDataEntity, NewDataEntity {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): NewCustomDataEntity
}

/**
 * A [DataEntity] that has already been inserted into the database.
 */
interface ExistingCustomDataEntity : CustomDataEntity, ExistingDataEntity {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ExistingCustomDataEntity
}

/**
 * An immutable [CustomDataEntity].
 */
// Intentionally not sealed so that consumers can define their own implementations.
interface ImmutableCustomDataEntity : CustomDataEntity, ImmutableDataEntity {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ImmutableCustomDataEntity
}

/**
 * An [ImmutableCustomDataEntity] with a mutable type.
 */
// Intentionally not sealed so that consumers can define their own implementations.
interface ImmutableCustomDataEntityWithMutableType<T : MutableCustomDataEntity> :
    ImmutableCustomDataEntity, ImmutableDataEntityWithMutableType<T> {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ImmutableCustomDataEntityWithMutableType<T>
}

/**
 * A mutable [CustomDataEntity].
 */
// Intentionally not sealed so that consumers can define their own implementations.
interface MutableCustomDataEntity : CustomDataEntity, MutableDataEntity {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableCustomDataEntity
}

/**
 * A [MutableCustomDataEntity], with a mutable [type] and [label].
 */
// Intentionally not sealed so that consumers can define their own implementations.
interface MutableCustomDataEntityWithTypeAndLabel<T : DataEntity.Type> : MutableCustomDataEntity,
    MutableDataEntityWithTypeAndLabel<T>, CustomDataEntityWithTypeAndLabel<T> {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableCustomDataEntityWithTypeAndLabel<T>
}