package contacts.entities.custom.handlename

import contacts.core.entities.MimeType
import contacts.core.entities.custom.ImmutableCustomData
import contacts.core.entities.custom.MutableCustomData
import contacts.core.entities.propertiesAreAllNullOrBlank
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
@Parcelize
data class HandleName internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The handle name.
     */
    val handle: String?

) : ImmutableCustomData {

    @IgnoredOnParcel
    override val mimeType: MimeType.Custom = HandleNameMimeType

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(handle)

    fun toMutableHandleName() = MutableHandleName(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        handle = handle
    )
}

/**
 * A mutable [HandleName].
 */
@Parcelize
data class MutableHandleName internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * See [HandleName.handle]
     */
    var handle: String?

) : MutableCustomData {

    constructor() : this(null, null, null, false, false, null)

    @IgnoredOnParcel
    override val mimeType: MimeType.Custom = HandleNameMimeType

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(handle)

    internal fun toHandleName() = HandleName(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        handle = handle
    )

    @IgnoredOnParcel
    override var primaryValue: String? by this::handle
}