package contacts.entities

import kotlinx.android.parcel.Parcelize

/**
 * This class does not have any real functional value. This exist only to prevent RawContacts from
 * being considered blanks, which may result in unwanted deletion in updates. Instances of this have
 * no data but is never blank.
 *
 * Consumers may use the ContactPhoto and RawContactPhoto extension functions to get/set photos.
 *
 * ## Developer notes
 *
 * Note that this is a class instead of an object to prevent future internal and consumer side
 * refactorings in case we need to add state.
 */
@Parcelize
class Photo : CommonDataEntity {

    override val mimeType: MimeType = MimeType.Photo

    override val id: Long? = null

    override val rawContactId: Long? = null

    override val contactId: Long? = null

    override val isPrimary: Boolean = false

    override val isSuperPrimary: Boolean = false

    // Flag as not blank so that the parent Contact or RawContact is also flagged as not blank.
    override val isBlank: Boolean = false
}