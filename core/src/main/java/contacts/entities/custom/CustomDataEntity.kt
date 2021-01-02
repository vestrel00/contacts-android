package contacts.entities.custom

import contacts.entities.CommonDataEntity
import contacts.entities.MimeType
import contacts.entities.MutableCommonDataEntity

/**
 * A custom [CommonDataEntity].
 *
 * Implementations are required to be parcelable. Kotlin users are recommended to use data class
 * combined with [kotlinx.android.parcel.Parcelize].
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
 *
 * ## Developer notes
 *
 * This does not also inherit from [CustomDataEntity] even though it is possible. This is done to
 * promote clear separation between the immutable and mutable entities, which follow the pattern of
 * the entire API. This prevents developer error where a [CustomDataEntity] is expected but a
 * [MutableCustomDataEntity] is produced or vice versa. Given that there is really no relationship
 * between the two, this must be prevented in order to prevent bugs at runtime. This is not
 * possible; CustomDataEntity<T: MutableCustomDataEntity>,
 * MutableCustomDataEntity<T: CustomDataEntity>
 */
interface MutableCustomDataEntity : /* CustomDataEntity, */ MutableCommonDataEntity {

    // Override this to cast type from MimeType to MimeType.Custom
    override val mimeType: MimeType.Custom
}