package contacts.core.entities.custom

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CustomDataEntityHolder(
    // This is not exposed to consumers so we do not need a generic type since we have no visibility
    // of consumer types anyways.
    val entities: MutableList<MutableCustomData>,
    val countRestriction: CustomDataCountRestriction
) : Parcelable

/* We'll go with non-sealed class approach because we don't need to expose these implementations.
   Consumers only need to specify if a data entity is either one-per-RawContact or
   one-or-more-per-RawContact. The above approach also leads to simpler code.
/**
 * Holds a single, one-per-RawContact entity or multiple, one-or-more-per-RawContact entities.
 */
internal sealed class CustomDataEntityHolder

/**
 * Holds zero or one-per-RawContact .
 */
internal class SingleCustomDataEntityHolder(
    val entity: MutableCustomDataEntity
) : CustomDataHolder()

/**
 * Holds zero or one-or-more-per-RawContact entities.
 */
class MultipleCustomDataEntityHolder(
    val entities: List<MutableCustomDataEntity>
) : CustomDataHolder()

val CustomDataEntityHolder.entities: List<MutableCustomDataEntity>
    get() = when (this) {
        is SingleCustomDataEntityHolder -> listOf(entity)
        is MultipleCustomDataEntityHolder -> entities

 */