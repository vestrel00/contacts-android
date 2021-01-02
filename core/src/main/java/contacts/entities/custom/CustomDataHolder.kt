package contacts.entities.custom

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CustomDataHolder(
    // This is not exposed to consumers so we do not need a generic type since we have no visibility
    // of consumer types anyways.
    val entities: MutableList<MutableCustomDataEntity>,
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
internal class SingleCustomDataHolder(
    val entity: MutableCustomData
) : CustomDataHolder()

/**
 * Holds zero or one-or-more-per-RawContact entities.
 */
class MultipleCustomDataHolder(
    val entities: List<MutableCustomDataEntity>
) : CustomDataHolder()

val CustomDataHolder.entityList: List<MutableCustomDataEntity>
    get() = when (this) {
        is SingleCustomDataHolder -> listOf(entity)
        is MultipleCustomDataHolder -> entities

 */