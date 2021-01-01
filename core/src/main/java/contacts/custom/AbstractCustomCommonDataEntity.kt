package contacts.custom

import contacts.entities.CommonDataEntity
import contacts.entities.MimeType
import contacts.entities.propertiesAreAllNullOrBlank as propertiesAreAllNullOrBlankImpl


/**
 * An abstract class that is used as a base of all custom [CommonDataEntity]s.
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