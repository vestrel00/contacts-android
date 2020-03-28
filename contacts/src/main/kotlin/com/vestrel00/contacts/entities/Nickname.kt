package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Nickname internal constructor(

    override val id: Long,

    override val rawContactId: Long,

    override val contactId: Long,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    // Type and Label are also available. However, the type keep getting set to default
    // automatically by the Contacts Provider...

    /**
     * The name itself.
     */
    val name: String?

) : DataEntity, Parcelable {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.NICKNAME

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(name)

    fun toMutableNickname() = MutableNickname(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        name = name
    )
}

@Parcelize
data class MutableNickname internal constructor(

    override val id: Long,

    override val rawContactId: Long,

    override val contactId: Long,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Nickname.name].
     */
    var name: String?

) : DataEntity, Parcelable {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.NICKNAME

    constructor() : this(INVALID_ID, INVALID_ID, INVALID_ID, false, false, null)

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(name)
}