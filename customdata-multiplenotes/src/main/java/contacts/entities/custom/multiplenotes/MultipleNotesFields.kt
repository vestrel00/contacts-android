package contacts.entities.custom.multiplenotes

import contacts.core.AbstractCustomDataField
import contacts.core.AbstractCustomDataField.ColumnName
import contacts.core.AbstractCustomDataFieldSet
import contacts.core.entities.MimeType

@ConsistentCopyVisibility
data class MultipleNotesField internal constructor(private val columnName: ColumnName) :
    AbstractCustomDataField(columnName) {

    override val customMimeType: MimeType.Custom = MultipleNotesMimeType
}

object MultipleNotesFields : AbstractCustomDataFieldSet<MultipleNotesField>() {

    // The ColumnName.DATA has the same value as CommonDataKinds.Note.NOTE; "data1"
    @JvmField
    val Note = MultipleNotesField(ColumnName.DATA)

    override val all: Set<MultipleNotesField> = setOf(Note)

    /**
     * The [Note] may be used for matching.
     *
     * See [AbstractCustomDataFieldSet.forMatching] for more info.
     */
    override val forMatching: Set<MultipleNotesField> = setOf(Note)

    /**
     * Same as [all], but as a function. This mainly exists for Java support. This makes it visible
     * to Java consumers when accessing this using the object reference directly.
     */
    @JvmStatic
    fun all() = all

    /**
     * Same as [forMatching], but as a function. This makes it visible to Java consumers when
     * accessing this using the object reference directly.
     */
    @JvmStatic
    fun forMatching() = forMatching
}