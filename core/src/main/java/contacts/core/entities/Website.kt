package contacts.core.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a website related to the contact.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 */
sealed interface WebsiteEntity : DataEntity {

    // Type and Label are also available. However, both keep getting set to null automatically by
    // the Contacts Provider...

    /**
     * The website URL string.
     */
    val url: String?

    override val mimeType: MimeType
        get() = MimeType.Website

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(url)
}

/**
 * An immutable [WebsiteEntity].
 */
@Parcelize
data class Website internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val url: String?

) : WebsiteEntity, ImmutableDataEntityWithMutableType<MutableWebsite> {

    override fun mutableCopy() = MutableWebsite(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        url = url
    )
}

/**
 * A mutable [WebsiteEntity].
 */
@Parcelize
data class MutableWebsite internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var url: String?

) : WebsiteEntity, MutableDataEntity {

    constructor() : this(null, null, null, false, false, null)

    @IgnoredOnParcel
    override var primaryValue: String? by this::url
}