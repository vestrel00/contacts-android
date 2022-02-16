package contacts.entities.custom.rpg.profession

import contacts.core.entities.custom.CustomDataFieldMapper
import contacts.entities.custom.rpg.RpgProfessionField
import contacts.entities.custom.rpg.RpgFields

internal class RpgProfessionFieldMapper :
    CustomDataFieldMapper<RpgProfessionField, RpgProfessionEntity> {

    override fun valueOf(
        field: RpgProfessionField,
        customDataEntity: RpgProfessionEntity
    ): String? =
        when (field) {
            RpgFields.Profession.Title -> customDataEntity.title
            else -> throw RpgProfessionDataException("Unrecognized rpg profession field $field")
        }
}