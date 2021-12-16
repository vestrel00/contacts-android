package contacts.entities.custom.handlename

import contacts.core.entities.*
import kotlinx.parcelize.Parcelize

/**
 * What is a handle name? In the online world, a handle is another word for a username. It can refer
 * to the name you use in chat rooms, web forums, and social media services like Twitter.
 *
 * https://techterms.com/definition/handle
 *
 * This is different from a nick name. Stay hip!
 *
 * A RawContact may have multiple handle names.
 */
sealed interface HandleNameEntity : CustomDataEntity {

    /**
     * The handle name.
     */
    val handle: String?

    /**
     * The [handle].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::handle
    override val primaryValue: String?
        get() = handle

    override val mimeType: MimeType.Custom
        get() = HandleNameMimeType

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(handle)
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from HandleNameEntity, there is only one interface that extends it; MutableHandleNameEntity.
 *
 * The MutableHandleNameEntity interface is used for library constructs that require an HandleNameEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableHandleName and NewHandleName. With this, we can create constructs that can
 * keep a reference to MutableHandleName(s) or NewHandleName(s) through the MutableHandleNameEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewHandleNameEntity, ExistingHandleNameEntity, and
 * ImmutableHandleNameEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [HandleNameEntity]. `
 */
sealed interface MutableHandleNameEntity : HandleNameEntity, MutableCustomDataEntity {

    override var handle: String?

    /**
     * The [handle].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::handle
    override var primaryValue: String?
        get() = handle
        set(value) {
            handle = value
        }
}

/**
 * An existing immutable [HandleNameEntity].
 */
@Parcelize
data class HandleName internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val handle: String?

) : HandleNameEntity, ExistingCustomDataEntity,
    ImmutableCustomDataEntityWithMutableType<MutableHandleName> {

    override fun mutableCopy() = MutableHandleName(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        handle = handle
    )
}

/**
 * An existing mutable [HandleNameEntity].
 */
@Parcelize
data class MutableHandleName internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var handle: String?

) : HandleNameEntity, ExistingCustomDataEntity, MutableHandleNameEntity


/**
 * A new mutable [HandleNameEntity].
 */
@Parcelize
data class NewHandleName @JvmOverloads constructor(

    override var handle: String? = null

) : HandleNameEntity, NewCustomDataEntity, MutableHandleNameEntity
