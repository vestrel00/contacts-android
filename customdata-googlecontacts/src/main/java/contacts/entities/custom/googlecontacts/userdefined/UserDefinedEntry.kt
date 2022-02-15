package contacts.entities.custom.googlecontacts.userdefined

import contacts.core.entities.custom.CustomDataRegistry.Entry
import contacts.entities.custom.googlecontacts.GoogleContactsFields
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType
import contacts.entities.custom.googlecontacts.UserDefinedField

// Keep this internal. Consumers don't need to see this stuff. Less visibility the better!
internal class UserDefinedEntry :
    Entry<UserDefinedField, UserDefinedDataCursor, UserDefinedEntity, UserDefined> {

    override val mimeType = GoogleContactsMimeType.UserDefined

    override val fieldSet = GoogleContactsFields.UserDefined

    override val fieldMapper = UserDefinedFieldMapper()

    override val countRestriction = USER_DEFINED_COUNT_RESTRICTION

    override val mapperFactory = UserDefinedMapperFactory()

    override val operationFactory = UserDefinedOperationFactory()
}