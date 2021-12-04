package contacts.core.entities.custom

import android.os.Parcelable
import contacts.core.entities.CustomDataEntity
import contacts.core.entities.ImmutableCustomDataEntity
import contacts.core.entities.ImmutableCustomDataEntityWithMutableType
import contacts.core.entities.ImmutableCustomDataEntityWithNullableMutableType
import kotlinx.parcelize.Parcelize

/**
 * Holds a **mutable** list of **immutable and/or mutable** custom data entities.
 *
 * Consumers should not use this directly.
 *
 * ## Dev notes
 *
 * This should actually be internal as it is of no use to consumers but this is referenced in an
 * interface (RawContactEntity)... At least we can make the the properties internal.
 */
sealed class AbstractCustomDataEntityHolder {

    internal abstract val entities: List<CustomDataEntity>

    internal abstract val countRestriction: CustomDataCountRestriction
}

/**
 * Holds a **mutable** list of **immutable** custom data entities.
 *
 * Consumers should not use this directly.
 *
 * ## Dev notes
 *
 * This should actually be internal as it is of no use to consumers but this is referenced in an
 * interface (RawContactEntity)... At least we can make the the properties internal.
 */
@Parcelize
data class ImmutableCustomDataEntityHolder(
    override val entities: MutableList<ImmutableCustomDataEntity>,
    override val countRestriction: CustomDataCountRestriction
) : AbstractCustomDataEntityHolder(), Parcelable {

    fun toCustomDataEntityHolder() = CustomDataEntityHolder(
        entities.toMutableListOfCustomDataEntity(),
        countRestriction
    )
}

private fun MutableList<ImmutableCustomDataEntity>.toMutableListOfCustomDataEntity()
        : MutableList<CustomDataEntity> = asSequence()
    .map {
        when (it) {
            is ImmutableCustomDataEntityWithMutableType<*> -> it.mutableCopy()
            is ImmutableCustomDataEntityWithNullableMutableType<*> -> {
                // For entities with no mutable type, just pass in the immutable reference.
                it.mutableCopy() ?: it
            }
            else -> it
        }
    }
    .toMutableList()

/**
 * Holds a **mutable** list of **immutable and/or mutable** custom data entities.
 *
 * Consumers should not use this directly.
 *
 * ## Dev notes
 *
 * This should actually be internal as it is of no use to consumers but this is referenced in an
 * interface (RawContactEntity)... At least we can make the the properties internal.
 */
@Parcelize
data class CustomDataEntityHolder(
    override val entities: MutableList<CustomDataEntity>,
    override val countRestriction: CustomDataCountRestriction
) : AbstractCustomDataEntityHolder(), Parcelable

/* We'll go with the above approach because we don't need to expose these implementations.
   Consumers only need to specify if a data entity is either one-per-RawContact or
   one-or-more-per-RawContact. The above approach also leads to simpler code.
/**
 * Holds a single, one-per-RawContact entity or multiple, one-or-more-per-RawContact entities.
 */
internal sealed interface CustomDataEntityHolder

/**
 * Holds zero or one-per-RawContact .
 */
internal class SingleCustomDataEntityHolder(
    val entity: CustomDataEntity
) : CustomDataHolder

/**
 * Holds zero or one-or-more-per-RawContact entities.
 */
class MultipleCustomDataEntityHolder(
    val entities: List<CustomDataEntity>
) : CustomDataHolder

val CustomDataEntityHolder.entities: List<CustomDataEntity>
    get() = when (this) {
        is SingleCustomDataEntityHolder -> listOf(entity)
        is MultipleCustomDataEntityHolder -> entities

 */