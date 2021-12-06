package contacts.core.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Notes about the contact.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 */
sealed interface NoteEntity : DataEntity {

    /**
     * The note.
     */
    val note: String?

    override val mimeType: MimeType
        get() = MimeType.Note

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(note)
}

/**
 * An immutable [NoteEntity].
 */
@Parcelize
data class Note internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val note: String?

) : NoteEntity, ImmutableDataEntityWithMutableType<MutableNote> {

    override fun mutableCopy() = MutableNote(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        note = note
    )
}

/**
 * A mutable [NoteEntity].
 */
@Parcelize
data class MutableNote internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var note: String?

) : NoteEntity, MutableDataEntity {

    constructor() : this(null, null, null, false, false, null)

    @IgnoredOnParcel
    override var primaryValue: String? by this::note
}