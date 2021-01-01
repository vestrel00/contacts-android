package contacts.entities.operation

import contacts.CommonDataField
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableNote

internal class NoteOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableNote>(isProfile) {

    override val mimeType = MimeType.Note

    override fun setData(
        data: MutableNote, setValue: (field: CommonDataField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Note.Note, data.note)
    }
}