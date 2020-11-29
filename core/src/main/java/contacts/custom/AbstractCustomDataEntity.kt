package contacts.custom

import contacts.entities.CommonDataEntity
import contacts.entities.propertiesAreAllNullOrBlank as propertiesAreAllNullOrBlankImpl


/**
 * An abstract [CommonDataEntity] that may be used as a base. This is optional. Consumers may
 * implement the [CommonDataEntity] directly.
 *
 * This is useful for Java consumers as it provides getters instead of having to implement it. It
 * also provides some useful functions available only to subclasses.
 */
abstract class AbstractCustomDataEntity(
    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,
    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean
) : CommonDataEntity {

    protected fun propertiesAreAllNullOrBlank(vararg properties: Any?): Boolean =
        propertiesAreAllNullOrBlankImpl(properties)
}