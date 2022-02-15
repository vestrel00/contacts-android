package contacts.entities.custom.googlecontacts.fileas

import contacts.core.entities.custom.CustomDataFieldMapper
import contacts.entities.custom.googlecontacts.FileAsField
import contacts.entities.custom.googlecontacts.GoogleContactsFields

internal class FileAsFieldMapper : CustomDataFieldMapper<FileAsField, FileAsEntity> {

    override fun valueOf(field: FileAsField, customDataEntity: FileAsEntity): String? =
        when (field) {
            GoogleContactsFields.FileAs.Name -> customDataEntity.name
            else -> throw FileAsDataException("Unrecognized file as field $field")
        }
}