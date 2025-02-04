package contacts.entities.custom.multiplenotes

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class MultipleNotesOperationFactory :
    AbstractCustomDataOperation.Factory<MultipleNotesField, MultipleNotesEntity> {

    override fun create(
        callerIsSyncAdapter: Boolean, isProfile: Boolean, includeFields: Set<MultipleNotesField>?
    ): AbstractCustomDataOperation<MultipleNotesField, MultipleNotesEntity> =
        MultipleNotesOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields = includeFields
        )
}

private class MultipleNotesOperation(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<MultipleNotesField>?
) : AbstractCustomDataOperation<MultipleNotesField, MultipleNotesEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    override val mimeType: MimeType.Custom = MultipleNotesMimeType

    override fun setCustomData(
        data: MultipleNotesEntity, setValue: (field: MultipleNotesField, value: Any?) -> Unit
    ) {
        setValue(MultipleNotesFields.Note, data.note)
    }
}