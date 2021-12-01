package contacts.core.entities

// Note that this is declared here and not in the entities.custom package because DataEntity is sealed here.
// If Kotlin increases the scope of "sealed" from package to module, then we can move it back =)

/**
 * A custom [DataEntity].
 *
 * Implementations are required to be parcelable. Kotlin users are recommended to use data class
 * combined with [kotlinx.parcelize.Parcelize].
 */
interface CustomDataEntity : DataEntity {

    // Override this to cast type from MimeType to MimeType.Custom
    override val mimeType: MimeType.Custom
}

/**
 * An immutable [CustomDataEntity].
 *
 * Implementors should define a toMutableX() function to allow for changes in their custom entities.
 */
interface ImmutableCustomData : CustomDataEntity, ImmutableData

/**
 * A mutable [CustomDataEntity].
 */
interface MutableCustomData : CustomDataEntity, MutableData

/**
 * A custom [MutableDataWithType].
 */
interface MutableCustomDataWithType<T : DataEntity.Type> : MutableCustomData, MutableDataWithType<T>