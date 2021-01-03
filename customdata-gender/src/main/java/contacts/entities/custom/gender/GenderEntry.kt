package contacts.entities.custom.gender

import contacts.entities.custom.CustomDataRegistry
import contacts.entities.custom.CustomDataRegistry.Entry

internal object GenderEntryId : CustomDataRegistry.EntryId

// Keep this internal. Consumers don't need to see this stuff. Less visibility the better!
internal class GenderEntry : Entry<GenderField, GenderDataCursor, MutableGender> {

    override val id = GenderEntryId

    override val mimeType = GenderMimeType

    override val fieldSet = GenderFields

    override val fieldMapper = GenderFieldMapper()

    override val countRestriction = GENDER_COUNT_RESTRICTION

    override val mapperFactory = GenderMapperFactory()

    override val operationFactory = GenderDataOperationFactory()
}