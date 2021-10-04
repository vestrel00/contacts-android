package contacts.entities.custom.handlename

import contacts.core.entities.custom.CustomDataRegistry.Entry

// Keep this internal. Consumers don't need to see this stuff. Less visibility the better!
internal class HandleNameEntry : Entry<HandleNameField, HandleNameDataCursor, MutableHandleName> {

    override val mimeType = HandleNameMimeType

    override val fieldSet = HandleNameFields

    override val fieldMapper = HandleNameFieldMapper()

    override val countRestriction = HANDLE_NAME_COUNT_RESTRICTION

    override val mapperFactory = HandleNameMapperFactory()

    override val operationFactory = HandleNameDataOperationFactory()
}