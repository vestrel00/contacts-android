package contacts.entities.custom.googlecontacts.fileas

import contacts.core.entities.custom.CustomDataRegistry.Entry
import contacts.entities.custom.googlecontacts.FileAsField
import contacts.entities.custom.googlecontacts.GoogleContactsFields
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType

// Keep this internal. Consumers don't need to see this stuff. Less visibility the better!
internal class FileAsEntry : Entry<FileAsField, FileAsDataCursor, FileAsEntity, FileAs> {

    override val mimeType = GoogleContactsMimeType.FileAs

    override val fieldSet = GoogleContactsFields.FileAs

    override val fieldMapper = FileAsFieldMapper()

    override val countRestriction = FILE_AS_COUNT_RESTRICTION

    override val mapperFactory = FileAsMapperFactory()

    override val operationFactory = FileAsOperationFactory()
}