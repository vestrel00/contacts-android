package contacts.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

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
    override val mimeType: MimeType = MimeType.Note

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(note)

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
    override val mimeType: MimeType = MimeType.Note

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(note)

    override var primaryValue: String?
        get() = note
        set(value) {
            note = value
        }
}