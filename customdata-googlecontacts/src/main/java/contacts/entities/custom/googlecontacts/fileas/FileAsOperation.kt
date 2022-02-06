package contacts.entities.custom.googlecontacts.fileas

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation
import contacts.entities.custom.googlecontacts.FileAsField
import contacts.entities.custom.googlecontacts.GoogleContactsFields
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType

internal class FileAsOperationFactory :
    AbstractCustomDataOperation.Factory<FileAsField, FileAsEntity> {

    override fun create(
        isProfile: Boolean, includeFields: Set<FileAsField>
    ): AbstractCustomDataOperation<FileAsField, FileAsEntity> =
        FileAsOperation(isProfile, includeFields)
}

private class FileAsOperation(isProfile: Boolean, includeFields: Set<FileAsField>) :
    AbstractCustomDataOperation<FileAsField, FileAsEntity>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = GoogleContactsMimeType.FileAs

    override fun setCustomData(
        data: FileAsEntity, setValue: (field: FileAsField, value: Any?) -> Unit
    ) {
        setValue(GoogleContactsFields.FileAs.Name, data.name)
    }
}