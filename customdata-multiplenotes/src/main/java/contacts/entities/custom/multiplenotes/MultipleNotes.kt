package contacts.entities.custom.multiplenotes

import contacts.core.entities.CustomDataEntity
import contacts.core.entities.ExistingCustomDataEntity
import contacts.core.entities.ImmutableCustomDataEntityWithMutableType
import contacts.core.entities.MimeType
import contacts.core.entities.MutableCustomDataEntity
import contacts.core.entities.NewCustomDataEntity
import contacts.core.entities.propertiesAreAllNullOrBlank
import kotlinx.parcelize.Parcelize

/**
 * This is the custom version of the built-in NoteEntity.
 *
 * This custom data **overrides the built-in NoteEntity**.
 *
 * **Currently**, the only difference is that a RawContact may have 0, 1, or more entries of this
 * custom data kind compared to the 0 or 1 entry of the built-in version.
 *
 * ## Developer notes
 *
 * The naming of this may confuse some people. Instances of this does not contain multiple notes
 * via a list of strings. Instances of this still contains a singular [note] string. Perhaps a
 * better name for this could be `CustomNoteEntity` or `OverriddenNoteEntity` but it does not follow
 * the nomenclature of the custom data "multiple notes" :D
 */
sealed interface MultipleNotesEntity : CustomDataEntity {

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

    override val mimeType: MimeType.Custom
        get() = MultipleNotesMimeType

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(note)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MultipleNotesEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from MultipleNotesEntity, there is only one interface that extends it; MutableMultipleNotesEntity.
 *
 * The MutableMultipleNotesEntity interface is used for library constructs that require an MultipleNotesEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableMultipleNotes and NewMultipleNotes. With this, we can create constructs that can
 * keep a reference to MutableMultipleNotes(s) or NewMultipleNotes(s) through the MutableMultipleNotesEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewMultipleNotesEntity, ExistingMultipleNotesEntity, and
 * ImmutableMultipleNotesEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [MultipleNotesEntity]. `
 */
sealed interface MutableMultipleNotesEntity : MultipleNotesEntity, MutableCustomDataEntity {

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
    override fun redactedCopy(): MutableMultipleNotesEntity
}

/**
 * An existing immutable [MultipleNotesEntity].
 */
@ConsistentCopyVisibility
@Parcelize
data class MultipleNotes internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val note: String?,

    override val isRedacted: Boolean

) : MultipleNotesEntity, ExistingCustomDataEntity,
    ImmutableCustomDataEntityWithMutableType<MutableMultipleNotes> {

    override fun mutableCopy() = MutableMultipleNotes(
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
 * An existing mutable [MultipleNotesEntity].
 */
@ConsistentCopyVisibility
@Parcelize
data class MutableMultipleNotes internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var note: String?,

    override val isRedacted: Boolean

) : MultipleNotesEntity, ExistingCustomDataEntity, MutableMultipleNotesEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        note = note?.redact()
    )
}

/**
 * A new mutable [MultipleNotesEntity].
 */
@Parcelize
data class NewMultipleNotes @JvmOverloads constructor(

    override var note: String? = null,

    override var isReadOnly: Boolean = false,
    override val isRedacted: Boolean = false

) : MultipleNotesEntity, NewCustomDataEntity, MutableMultipleNotesEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        note = note?.redact()
    )
}
