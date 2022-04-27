package contacts.core.entities

import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a photo for the (raw) contact.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 *
 * Consumers may use the ContactPhoto and RawContactPhoto extension functions to get/set/remove
 * photos and thumbnails.
 */
// I know this interface is not necessary because there is only one implementation. Still, it does
// not hurt to have it. It follows the setup like everything else, so it's cool.
sealed interface PhotoEntity : DataEntity {

    /**
     * Photo file ID for the display photo of the raw contact.
     *
     * This is for advanced usage only. Use the ContactPhoto and RawContactPhoto extension functions
     * to get/set/remove photos and thumbnails.
     */
    val fileId: Long?

    /**
     * Unused and will always return null.
     */
    override val primaryValue: String?
        get() = null

    override val mimeType: MimeType
        get() = MimeType.Photo

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(fileId)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): PhotoEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * This is why there are no interfaces for NewPhotoEntity, ExistingPhotoEntity,
 * ImmutablePhotoEntity, and MutableNewPhotoEntity. There are currently no library functions or
 * constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * An existing immutable [PhotoEntity].
 */
@Parcelize
data class Photo internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val fileId: Long?,

    override val isRedacted: Boolean

) : PhotoEntity, ExistingDataEntity, ImmutableDataEntity {

    // Nothing to redact.
    override fun redactedCopy() = copy(isRedacted = true)
}