package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.NoteField
import contacts.core.entities.MimeType
import contacts.core.entities.NoteEntity

internal class NoteOperation(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<NoteField>
) : AbstractDataOperation<NoteField, NoteEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    override val mimeType = MimeType.Note

    override fun setValuesFromData(
        data: NoteEntity, setValue: (field: NoteField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Note.Note, data.note)
    }
}