package contacts.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Nickname internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    // Type and Label are also available. However, the type keep getting set to default
    // automatically by the Contacts Provider...

    /**
     * The name itself.
     */
    val name: String?

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Nickname

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(name)

    fun toMutableNickname() = MutableNickname(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        name = name
    )
}

@Parcelize
data class MutableNickname internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Nickname.name].
     */
    var name: String?

) : MutableCommonDataEntity {

    constructor() : this(null, null, null, false, false, null)

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Nickname

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(name)
}