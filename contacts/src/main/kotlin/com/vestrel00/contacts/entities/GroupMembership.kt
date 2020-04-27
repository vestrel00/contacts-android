package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

// Note that the actual Group lives in a separate table that is not joined with the Data table.
@Parcelize
data class GroupMembership internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The id of the Group in the Groups table that this membership refers to, which must share
     * the same account as the contact.
     *
     * This is a read-only attribute, which is ignored for insert, update, and delete functions.
     */
    val groupId: Long?

) : DataEntity, Parcelable {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.GROUP_MEMBERSHIP

    override fun isBlank(): Boolean = false
}

// NOTE. The GroupMembership class intentionally does not have a mutable version unlike the other
// entities. Manage the group memberships via the RawContactGroupMemberships extension functions.