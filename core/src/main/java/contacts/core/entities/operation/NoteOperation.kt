package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.NoteField
import contacts.core.entities.MimeType
import contacts.core.entities.MutableNote

internal class NoteOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<NoteField, MutableNote>(isProfile) {

    override val mimeType = MimeType.Note

    override fun setData(
        data: MutableNote, setValue: (field: NoteField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Note.Note, data.note)
    }
}