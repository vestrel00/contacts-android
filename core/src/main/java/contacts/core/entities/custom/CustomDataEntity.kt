package contacts.core.entities.custom

import contacts.core.entities.CommonDataEntity
import contacts.core.entities.MimeType
import contacts.core.entities.MutableCommonDataEntity
import contacts.core.entities.MutableCommonDataEntityWithType

/**
 * A custom [CommonDataEntity].
 *
 * Implementations are required to be parcelable. Kotlin users are recommended to use data class
 * combined with [kotlinx.parcelize.Parcelize].
 *
 * Implementors should define a toMutableX() function to allow for changes in their custom entities.
 *
 * ## Developer notes
 *
 * Technically, this can be optional. We could have implemented this part of the API to be able to
 * handle [CommonDataEntity] directly instead of this [CustomDataEntity]. However, we are able to
 * streamline all custom entities this way, which makes our internal code easier to follow / trace.
 * It also gives us more control and flexibility.
 */
interface CustomDataEntity : CommonDataEntity {

    // Override this to cast type from MimeType to MimeType.Custom
    override val mimeType: MimeType.Custom
}

/**
 * A custom [MutableCommonDataEntity].
 */
interface MutableCustomDataEntity : CustomDataEntity, MutableCommonDataEntity

/**
 * A custom [MutableCommonDataEntityWithType].
 */
interface MutableCustomDataEntityWithType<T : CommonDataEntity.Type> :
    MutableCustomDataEntity, MutableCommonDataEntityWithType<T>