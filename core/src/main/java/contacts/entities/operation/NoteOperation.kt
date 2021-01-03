package contacts.entities.operation

import contacts.Fields
import contacts.NoteField
import contacts.entities.MimeType
import contacts.entities.MutableNote

internal class NoteOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<NoteField, MutableNote>(isProfile) {

    override val mimeType = MimeType.Note

    override fun setData(
        data: MutableNote, setValue: (field: NoteField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Note.Note, data.note)
    }
}