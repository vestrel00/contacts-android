package contacts.core.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing the contact's nickname.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 */
sealed interface NicknameEntity : DataEntity {

    // Type and Label are also available. However, the type keep getting set to default
    // automatically by the Contacts Provider...

    /**
     * The nickname
     */
    val name: String?

    override val mimeType: MimeType
        get() = MimeType.Nickname

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(name)
}

/**
 * An immutable [NicknameEntity].
 */
@Parcelize
data class Nickname internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val name: String?

) : NicknameEntity, ImmutableDataEntityWithMutableType<MutableNickname> {

    override fun mutableCopy() = MutableNickname(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        name = name
    )
}

/**
 * A mutable [NicknameEntity].
 */
@Parcelize
data class MutableNickname internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var name: String?

) : NicknameEntity, MutableDataEntity {

    constructor() : this(null, null, null, false, false, null)

    @IgnoredOnParcel
    override var primaryValue: String? by this::name
}