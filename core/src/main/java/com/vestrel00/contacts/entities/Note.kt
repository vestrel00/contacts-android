package com.vestrel00.contacts.entities

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Note internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The note text.
     */
    val note: String?

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.NOTE

    @IgnoredOnParcel
    override val isBlank: Boolean = propertiesAreAllNullOrBlank(note)

    fun toMutableNote() = MutableNote(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        note = note
    )
}

@Parcelize
data class MutableNote internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Note.note].
     */
    var note: String?

) : MutableCommonDataEntity {

    constructor() : this(null, null, null, false, false, null)

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.NOTE

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(note)
}