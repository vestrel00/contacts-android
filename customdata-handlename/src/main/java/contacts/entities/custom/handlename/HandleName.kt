package contacts.entities.custom.handlename

import contacts.core.entities.*
import kotlinx.parcelize.IgnoredOnParcel
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

    override val mimeType: MimeType.Custom
        get() = HandleNameMimeType

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(handle)
}

/**
 * An immutable [HandleNameEntity].
 */
@Parcelize
data class HandleName internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val handle: String?

) : HandleNameEntity, ImmutableCustomDataEntityWithMutableType<MutableHandleName> {

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
 * A mutable [HandleNameEntity].
 */
@Parcelize
data class MutableHandleName internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var handle: String?

) : HandleNameEntity, MutableCustomDataEntity {

    constructor() : this(null, null, null, false, false, null)

    @IgnoredOnParcel
    override var primaryValue: String? by this::handle
}