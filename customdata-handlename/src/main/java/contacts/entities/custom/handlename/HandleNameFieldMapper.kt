package contacts.entities.custom.handlename

import contacts.core.entities.custom.CustomDataFieldMapper

internal class HandleNameFieldMapper : CustomDataFieldMapper<HandleNameField, HandleNameEntity> {

    override fun valueOf(field: HandleNameField, customDataEntity: HandleNameEntity): String? =
        when (field) {
            HandleNameFields.Handle -> customDataEntity.handle
            else -> throw HandleNameDataException("Unrecognized handle name field $field")
        }
}