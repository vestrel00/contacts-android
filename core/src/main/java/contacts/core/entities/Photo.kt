package contacts.core.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a photo for the (raw) contact.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 *
 * This class does not have any real functional value. This exist only to prevent RawContacts from
 * being considered blanks, which may result in unwanted deletion in updates. Instances of this have
 * no data but is never blank.
 *
 * Consumers may use the ContactPhoto and RawContactPhoto extension functions to get/set/remove
 * photos and thumbnails.
 *
 * ## Dev notes
 *
 * This should actually be internal as it is of no use to consumers but this is referenced in an
 * interface (RawContactEntity)...
 */
// I know this interface is not necessary because there is only one implementation. Still, it does
// not hurt to have it. It follows the setup like everything else, so it's cool.
sealed interface PhotoEntity : DataEntity

/**
 * An immutable [PhotoEntity].
 */
@Parcelize
class Photo : PhotoEntity, ImmutableDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Photo

    @IgnoredOnParcel
    override val id: Long? = null

    @IgnoredOnParcel
    override val rawContactId: Long? = null

    @IgnoredOnParcel
    override val contactId: Long? = null

    @IgnoredOnParcel
    override val isPrimary: Boolean = false

    @IgnoredOnParcel
    override val isSuperPrimary: Boolean = false

    @IgnoredOnParcel
    // Flag as not blank so that the parent Contact or RawContact is also flagged as not blank.
    override val isBlank: Boolean = false
}