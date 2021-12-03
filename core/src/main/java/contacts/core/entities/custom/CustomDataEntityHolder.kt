package contacts.core.entities.custom

import android.os.Parcelable
import contacts.core.entities.CustomDataEntity
import kotlinx.parcelize.Parcelize

/**
 * Holds custom data entities the the API processes internally.
 *
 * Consumers do not and should not use this.
 *
 * ## Dev notes
 *
 * This should actually be internal as it is of no use to consumers but this is referenced in an
 * interface (RawContactEntity)... At least we can make
 */
@Parcelize
data class CustomDataEntityHolder(
    internal val entities: MutableList<CustomDataEntity>,
    internal val countRestriction: CustomDataCountRestriction
) : Parcelable

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