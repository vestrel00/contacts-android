package contacts.entities.custom.googlecontacts.userdefined

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation
import contacts.entities.custom.googlecontacts.UserDefinedField
import contacts.entities.custom.googlecontacts.GoogleContactsFields
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType

internal class UserDefinedOperationFactory :
    AbstractCustomDataOperation.Factory<UserDefinedField, UserDefinedEntity> {

    override fun create(
        isProfile: Boolean, includeFields: Set<UserDefinedField>
    ): AbstractCustomDataOperation<UserDefinedField, UserDefinedEntity> =
        UserDefinedOperation(isProfile, includeFields)
}

private class UserDefinedOperation(isProfile: Boolean, includeFields: Set<UserDefinedField>) :
    AbstractCustomDataOperation<UserDefinedField, UserDefinedEntity>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = GoogleContactsMimeType.UserDefined

    override fun setCustomData(
        data: UserDefinedEntity, setValue: (field: UserDefinedField, value: Any?) -> Unit
    ) {
        setValue(GoogleContactsFields.UserDefined.Field, data.field)
        setValue(GoogleContactsFields.UserDefined.Label, data.label)
    }
}