package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// Note that the actual Group lives in a separate table that is not joined with the Data table.
@Parcelize
data class GroupMembership internal constructor(

    /**
     * The id of this row in the Data table.
     */
    override val id: Long,

    /**
     * The id of the [RawContact] this data belongs to.
     */
    override val rawContactId: Long,

    /**
     * The id of the [Contact] that this data entity is associated with.
     */
    override val contactId: Long,

    /**
     * The id of the Group in the Groups table that this membership refers to, which must share
     * the same account as the contact.
     *
     * This is a read-only attribute, which is ignored for insert, update, and delete functions.
     */
    val groupId: Long

) : DataEntity, Parcelable