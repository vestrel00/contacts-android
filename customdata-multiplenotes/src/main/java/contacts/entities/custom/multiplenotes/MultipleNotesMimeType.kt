package contacts.entities.custom.multiplenotes

import contacts.core.entities.MimeType

internal data object MultipleNotesMimeType : MimeType.Custom() {

    // Use the same value as the built-in mime type for note to override it.
    override val value: String = "vnd.android.cursor.item/note"
}