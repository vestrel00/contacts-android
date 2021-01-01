package contacts.entities.custom

import contacts.entities.CommonDataEntity
import contacts.entities.MimeType
import contacts.entities.MutableCommonDataEntity
import contacts.entities.propertiesAreAllNullOrBlank as propertiesAreAllNullOrBlankImpl


/**
 * An abstract class that is used as a base of all custom [CommonDataEntity]s.
 *
 * Implementations are required to be parcelable. Kotlin users are recommended to use data class
 * combined with [kotlinx.android.parcel.Parcelize].
 *
 * Implementors should define a toMutableX() function to allow for changes in their custom entities.
 *
 * ## Developer notes
 *
 * Technically, this can be optional. We could have implemented this part of the API to be able to
 * handle [CommonDataEntity] directly instead of this [AbstractCustomCommonDataEntity]. However, we
 * are able to streamline all custom entities this way, which makes our internal code easier to
 * follow / trace. It also gives us more control and flexibility.
 */
abstract class AbstractCustomCommonDataEntity(
    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,
    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean
) : CommonDataEntity {

    // Override this to cast type from MimeType to MimeType.Custom
    abstract override val mimeType: MimeType.Custom

    protected fun propertiesAreAllNullOrBlank(vararg properties: Any?): Boolean =
        propertiesAreAllNullOrBlankImpl(properties)
}

/**
 * A mutable version of [AbstractCustomCommonDataEntity].
 */
abstract class AbstractMutableCustomCommonDataEntity(
    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,
    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean
) : MutableCommonDataEntity {

    // Override this to cast type from MimeType to MimeType.Custom
    abstract override val mimeType: MimeType.Custom

    protected fun propertiesAreAllNullOrBlank(vararg properties: Any?): Boolean =
        propertiesAreAllNullOrBlankImpl(properties)
}