package contacts.core.entities.custom

import android.os.Parcelable
import contacts.core.Redactable
import contacts.core.entities.CustomDataEntity
import contacts.core.entities.ImmutableCustomDataEntity
import contacts.core.entities.ImmutableCustomDataEntityWithMutableType
import contacts.core.redactedCopies
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
 *
 * This is not an interface because we will not be able to make things internal.
 */
sealed class AbstractCustomDataEntityHolder : Redactable {

    internal abstract val entities: List<CustomDataEntity>

    internal abstract val countRestriction: CustomDataCountRestriction

    // We have to cast the return type because we are not using recursive generic types.
    abstract override fun redactedCopy(): AbstractCustomDataEntityHolder
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
data class ImmutableCustomDataEntityHolder internal constructor(
    override val entities: MutableList<ImmutableCustomDataEntity>,
    override val countRestriction: CustomDataCountRestriction,

    override val isRedacted: Boolean
) : AbstractCustomDataEntityHolder(), Parcelable {

    fun mutableCopy() = CustomDataEntityHolder(
        entities.toMutableListOfCustomDataEntity(),
        countRestriction,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        entities = entities.asSequence().redactedCopies().toMutableList()
    )
}

private fun MutableList<ImmutableCustomDataEntity>.toMutableListOfCustomDataEntity()
        : MutableList<CustomDataEntity> = asSequence()
    .map {
        if (it is ImmutableCustomDataEntityWithMutableType<*>) {
            it.mutableCopy()
        } else {
            it
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
data class CustomDataEntityHolder internal constructor(
    override val entities: MutableList<CustomDataEntity>,
    override val countRestriction: CustomDataCountRestriction,

    override val isRedacted: Boolean
) : AbstractCustomDataEntityHolder(), Parcelable {

    override fun redactedCopy() = copy(
        isRedacted = true,

        entities = entities.asSequence().redactedCopies().toMutableList()
    )
}

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