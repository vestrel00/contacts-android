package contacts.entities.custom.rpg.profession

import contacts.core.entities.custom.CustomDataRegistry.Entry
import contacts.entities.custom.rpg.RpgFields
import contacts.entities.custom.rpg.RpgMimeType
import contacts.entities.custom.rpg.RpgProfessionField

// Keep this internal. Consumers don't need to see this stuff. Less visibility the better!
internal class RpgProfessionEntry :
    Entry<RpgProfessionField, RpgProfessionDataCursor, RpgProfessionEntity, RpgProfession> {

    override val mimeType = RpgMimeType.Profession

    override val fieldSet = RpgFields.Profession

    override val fieldMapper = RpgProfessionFieldMapper()

    override val countRestriction = RPG_PROFESSION_COUNT_RESTRICTION

    override val mapperFactory = RpgProfessionMapperFactory()

    override val operationFactory = RpgProfessionOperationFactory()
}