package contacts.entities.custom.googlecontacts.userdefined

import contacts.core.entities.custom.CustomDataFieldMapper
import contacts.entities.custom.googlecontacts.GoogleContactsFields
import contacts.entities.custom.googlecontacts.UserDefinedField

internal class UserDefinedFieldMapper : CustomDataFieldMapper<UserDefinedField, UserDefinedEntity> {

    override fun valueOf(field: UserDefinedField, customDataEntity: UserDefinedEntity): String? =
        when (field) {
            GoogleContactsFields.UserDefined.Field -> customDataEntity.field
            GoogleContactsFields.UserDefined.Label -> customDataEntity.label
            else -> throw UserDefinedDataException("Unrecognized user defined field $field")
        }
}