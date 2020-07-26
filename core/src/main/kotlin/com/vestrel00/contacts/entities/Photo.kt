package com.vestrel00.contacts.entities

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * This class does not have any real functional value. This exist only to prevent RawContacts from
 * being considered blanks, which may result in unwanted deletion in updates.
 *
 * Consumers may use the ContactPhoto and RawContactPhoto extension functions to get/set photos.
 */
@Parcelize
internal data class Photo(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.PHOTO

    // Flag as not blank so that the parent Contact or RawContact is also flagged as not blank.
    override fun isBlank(): Boolean = false
}