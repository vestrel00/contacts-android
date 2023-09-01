package contacts.core.entities

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

    /**
     * The [note].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::note
    override val primaryValue: String?
        get() = note

    override val mimeType: MimeType
        get() = MimeType.Note

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(note)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): NoteEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from NoteEntity, there is only one interface that extends it; MutableNoteEntity.
 *
 * The MutableNoteEntity interface is used for library constructs that require an NoteEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableNote and NewNote. With this, we can create constructs that can
 * keep a reference to MutableNote(s) or NewNote(s) through the MutableNoteEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewNoteEntity, ExistingNoteEntity, and
 * ImmutableNoteEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [NoteEntity]. `
 */
sealed interface MutableNoteEntity : NoteEntity, MutableDataEntity {

    override var note: String?

    /**
     * The [note].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::note
    override var primaryValue: String?
        get() = note
        set(value) {
            note = value
        }

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableNoteEntity
}

/**
 * An existing immutable [NoteEntity].
 */
@Parcelize
data class Note internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val note: String?,

    override val isRedacted: Boolean

) : NoteEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutableNote> {

    override fun mutableCopy() = MutableNote(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        note = note,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        note = note?.redact()
    )
}

/**
 * An existing mutable [NoteEntity].
 */
@Parcelize
data class MutableNote internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var note: String?,

    override val isRedacted: Boolean

) : NoteEntity, ExistingDataEntity, MutableNoteEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        note = note?.redact()
    )
}

/**
 * A new mutable [NoteEntity].
 */
@Parcelize
data class NewNote @JvmOverloads constructor(

    override var note: String? = null,

    override var isReadOnly: Boolean = false,
    override val isRedacted: Boolean = false

) : NoteEntity, NewDataEntity, MutableNoteEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        note = note?.redact()
    )
}