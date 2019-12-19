package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Nickname internal constructor(

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

    // Type and Label are also available. However, the type keep getting set to default
    // automatically by the Contacts Provider...

    /**
     * The name itself.
     */
    val name: String?

) : DataEntity, Parcelable {

    fun toMutableNickname() = MutableNickname(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        name = name
    )
}

@Parcelize
data class MutableNickname internal constructor(

    /**
     * See [Nickname.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * See [Nickname.rawContactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val rawContactId: Long,

    /**
     * See [Nickname.contactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val contactId: Long,

    /**
     * See [Nickname.name].
     */
    var name: String?

) : DataEntity, Parcelable {

    constructor() : this(INVALID_ID, INVALID_ID, INVALID_ID, null)

    internal fun toNickname() = Nickname(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        name = name
    )
}