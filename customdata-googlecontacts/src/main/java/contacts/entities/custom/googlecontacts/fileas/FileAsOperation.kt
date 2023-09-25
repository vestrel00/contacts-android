package contacts.entities.custom.googlecontacts.fileas

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation
import contacts.entities.custom.googlecontacts.FileAsField
import contacts.entities.custom.googlecontacts.GoogleContactsFields
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType

internal class FileAsOperationFactory :
    AbstractCustomDataOperation.Factory<FileAsField, FileAsEntity> {

    override fun create(
        callerIsSyncAdapter: Boolean,
        isProfile: Boolean,
        includeFields: Set<FileAsField>?
    ): AbstractCustomDataOperation<FileAsField, FileAsEntity> = FileAsOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        includeFields = includeFields
    )
}

private class FileAsOperation(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<FileAsField>?
) : AbstractCustomDataOperation<FileAsField, FileAsEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    override val mimeType: MimeType.Custom = GoogleContactsMimeType.FileAs

    override fun setCustomData(
        data: FileAsEntity, setValue: (field: FileAsField, value: Any?) -> Unit
    ) {
        setValue(GoogleContactsFields.FileAs.Name, data.name)
    }
}