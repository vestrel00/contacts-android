package contacts.entities.custom.gender

import contacts.core.entities.custom.CustomDataFieldMapper

internal class GenderFieldMapper : CustomDataFieldMapper<GenderField, GenderEntity> {

    override fun valueOf(field: GenderField, customDataEntity: GenderEntity): String? =
        when (field) {
            GenderFields.Type -> customDataEntity.type?.ordinal?.toString()
            GenderFields.Label -> customDataEntity.label
            else -> throw GenderDataException("Unrecognized gender field $field")
        }
}