package contacts.entities.custom

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class MutableCustomCommonDataEntityHolder(
    // This is not exposed to consumers so we do not need a generic type since we have no visibility
    // of consumer types anyways.
    val entities: MutableList<MutableCustomCommonDataEntity>,
    val countRestriction: CustomCommonDataEntityCountRestriction
) : Parcelable

/* We'll go with non-sealed class approach because we don't need to expose these implementations.
   Consumers only need to specify if a data entity is either one-per-RawContact or
   one-or-more-per-RawContact. The above approach also leads to simpler code.
/**
 * Holds a single, one-per-RawContact entity or multiple, one-or-more-per-RawContact entities.
 */
internal sealed class MutableCustomCommonDataEntityHolder

/**
 * Holds zero or one-per-RawContact entity.
 */
internal class SingleMutableCustomCommonDataEntityHolder(
    val entity: MutableCustomCommonDataEntity
) : MutableCustomCommonDataEntityHolder()

/**
 * Holds zero or one-or-more-per-RawContact entities.
 */
class MultipleMutableCustomCommonDataEntityHolder(
    val entities: List<MutableCustomCommonDataEntity>
) : MutableCustomCommonDataEntityHolder()

val MutableCustomCommonDataEntityHolder.entityList: List<MutableCustomCommonDataEntity>
    get() = when (this) {
        is SingleMutableCustomCommonDataEntityHolder -> listOf(entity)
        is MultipleMutableCustomCommonDataEntityHolder -> entities

 */