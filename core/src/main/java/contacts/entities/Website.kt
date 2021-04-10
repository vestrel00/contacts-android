package contacts.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Website internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    // Type and Label are also available. However, both keep getting set to null automatically by
    // the Contacts Provider...

    /**
     * The website URL string.
     */
    val url: String?

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Website

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(url)

    fun toMutableWebsite() = MutableWebsite(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        url = url
    )
}

@Parcelize
data class MutableWebsite internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Website.url].
     */
    var url: String?

) : MutableCommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Website

    constructor() : this(null, null, null, false, false, null)

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(url)
}