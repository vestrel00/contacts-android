package contacts.entities.custom.multiplenotes

import contacts.core.entities.custom.CustomDataFieldMapper

internal class MultipleNotesFieldMapper : CustomDataFieldMapper<MultipleNotesField, MultipleNotesEntity> {

    override fun valueOf(field: MultipleNotesField, customDataEntity: MultipleNotesEntity): String? =
        when (field) {
            MultipleNotesFields.Note -> customDataEntity.note
            else -> throw MultipleNotesDataException("Unrecognized note field $field")
        }
}