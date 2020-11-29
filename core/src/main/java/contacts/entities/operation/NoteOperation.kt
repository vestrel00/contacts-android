package contacts.entities.operation

import contacts.Field
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableNote

internal class NoteOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableNote>(isProfile) {

    override val mimeType = MimeType.NOTE

    override fun setData(
        data: MutableNote, setValue: (field: Field, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Note.Note, data.note)
    }
}