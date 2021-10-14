package contacts.core.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Notes about the contact.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
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

/**
 * A mutable [Note].
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
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

    @IgnoredOnParcel
    override var primaryValue: String? by this::note
}