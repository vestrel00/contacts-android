package contacts.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * This class does not have any real functional value. This exist only to prevent RawContacts from
 * being considered blanks, which may result in unwanted deletion in updates. Instances of this have
 * no data but is never blank.
 *
 * Consumers may use the ContactPhoto and RawContactPhoto extension functions to get/set photos.
 *
 * TODO Add the thumbnail here to seamlessly support showing thumbnails for each contact shown in
 * a list. This would be much faster and less CPU-intensive than using a get thumbnail function for
 * each contact shown in a list. Memory consumption is about the same. Do not include this in Fields.all.
 * It should be explicitly included in the query by the API user to prevent unintentional CPU and memory usage.
 *
 * ## Developer notes
 *
 * Note that this is a class instead of an object to prevent future internal and consumer side
 * refactorings in case we need to add state.
 */
@Parcelize
class Photo : CommonDataEntity {

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