package contacts.entities

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * This class does not have any real functional value. This exist only to prevent RawContacts from
 * being considered blanks, which may result in unwanted deletion in updates. Instances of this have
 * no data but is never blank.
 *
 * Consumers may use the ContactPhoto and RawContactPhoto extension functions to get/set photos.
 *
 * Note that this is a class instead of an object because @IgnoredOnParcel can not be applied to
 * object properties. Also, just in case we need to actually transform this into something more.
 */
@Parcelize
internal class Photo : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.PHOTO

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

    // Flag as not blank so that the parent Contact or RawContact is also flagged as not blank.
    @IgnoredOnParcel
    override val isBlank: Boolean = false
}