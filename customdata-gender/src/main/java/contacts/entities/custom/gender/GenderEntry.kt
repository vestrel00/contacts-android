package contacts.entities.custom.gender

import contacts.core.entities.custom.CustomDataRegistry.Entry

// Keep this internal. Consumers don't need to see this stuff. Less visibility the better!
internal class GenderEntry : Entry<GenderField, GenderDataCursor, GenderEntity, Gender> {

    override val mimeType = GenderMimeType

    override val fieldSet = GenderFields

    override val fieldMapper = GenderFieldMapper()

    override val countRestriction = GENDER_COUNT_RESTRICTION

    override val mapperFactory = GenderMapperFactory()

    override val operationFactory = GenderOperationFactory()
}