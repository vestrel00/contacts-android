package contacts.core.entities

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

    /**
     * The [url].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::url
    override val primaryValue: String?
        get() = url

    override val mimeType: MimeType
        get() = MimeType.Website

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(url)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): WebsiteEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from WebsiteEntity, there is only one interface that extends it; MutableWebsiteEntity.
 *
 * The MutableWebsiteEntity interface is used for library constructs that require an WebsiteEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableWebsite and NewWebsite. With this, we can create constructs that can
 * keep a reference to MutableWebsite(s) or NewWebsite(s) through the MutableWebsiteEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewWebsiteEntity, ExistingWebsiteEntity, and
 * ImmutableWebsiteEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [WebsiteEntity]. `
 */
sealed interface MutableWebsiteEntity : WebsiteEntity, MutableDataEntity {

    override var url: String?

    /**
     * The [url].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::url
    override var primaryValue: String?
        get() = url
        set(value) {
            url = value
        }

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableWebsiteEntity
}

/**
 * An existing immutable [WebsiteEntity].
 */
@Parcelize
data class Website internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val url: String?,

    override val isRedacted: Boolean

) : WebsiteEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutableWebsite> {

    override fun mutableCopy() = MutableWebsite(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        url = url,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        url = url?.redact(),

        isRedacted = true
    )
}

/**
 * An existing mutable [WebsiteEntity].
 */
@Parcelize
data class MutableWebsite internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var url: String?,

    override val isRedacted: Boolean

) : WebsiteEntity, ExistingDataEntity, MutableWebsiteEntity {

    override fun redactedCopy() = copy(
        url = url?.redact(),

        isRedacted = true
    )
}

/**
 * A new mutable [WebsiteEntity].
 */
@Parcelize
data class NewWebsite @JvmOverloads constructor(

    override var url: String? = null,

    override val isRedacted: Boolean = false

) : WebsiteEntity, NewDataEntity, MutableWebsiteEntity {

    override fun redactedCopy() = copy(
        url = url?.redact(),

        isRedacted = true
    )
}