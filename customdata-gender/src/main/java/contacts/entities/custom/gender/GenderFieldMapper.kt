package contacts.entities.custom.gender

import contacts.entities.custom.CustomDataFieldMapper

internal class GenderFieldMapper : CustomDataFieldMapper<GenderField, MutableGender> {

    override fun valueOf(field: GenderField, customEntity: MutableGender): String? = when (field) {
        GenderFields.Type -> customEntity.type?.ordinal?.toString()
        GenderFields.Label -> customEntity.label
        else -> throw GenderDataException("Unrecognized gender field $field")
    }
}