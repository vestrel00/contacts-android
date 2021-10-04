package contacts.entities.custom.handlename

import contacts.core.entities.custom.CustomDataFieldMapper

internal class HandleNameFieldMapper : CustomDataFieldMapper<HandleNameField, MutableHandleName> {

    override fun valueOf(field: HandleNameField, customEntity: MutableHandleName): String? =
        when (field) {
            HandleNameFields.Handle -> customEntity.handle
            else -> throw HandleNameDataException("Unrecognized handle name field $field")
        }
}