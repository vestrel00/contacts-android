package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Contact data specific to an [android.accounts.Account].
 *
 * ## Developer notes
 *
 * MutableLists are used instead of MutableSets to allow for duplicates, which are allowed in the
 * native Contacts app. Technically, sets could be used here because each data has different row id.
 * However, [MutableRawContact] data all have invalid ids, which disallows duplicates for consumer-
 * created data instances. Therefore, lists are also used here for parity. Besides, lists are more
 * commonly used in Android development and are more supported than sets.
 */
@Parcelize
data class RawContact internal constructor(

    /**
     * The unique ID of this [RawContact].
     *
     * The value of RawContacts._ID / Data.RAW_CONTACT_ID.
     */
    override val id: Long

) : Entity, Parcelable {

    fun toMutableRawContact() = MutableRawContact(
        id = id
    )
}

/**
 * A mutable [RawContact].
 */
@Parcelize
data class MutableRawContact internal constructor(

    /**
     * See [RawContact.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long

) : Entity, Parcelable {

    constructor() : this(INVALID_ID)

    internal fun toRawContact() = RawContact(
        id = id
    )
}