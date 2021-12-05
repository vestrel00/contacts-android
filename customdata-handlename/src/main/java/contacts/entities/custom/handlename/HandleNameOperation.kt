package contacts.entities.custom.handlename

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class HandleNameOperationFactory :
    AbstractCustomDataOperation.Factory<HandleNameField, HandleNameEntity> {

    override fun create(
        isProfile: Boolean, includeFields: Set<HandleNameField>
    ): AbstractCustomDataOperation<HandleNameField, HandleNameEntity> =
        HandleNameOperation(isProfile, includeFields)
}

private class HandleNameOperation(isProfile: Boolean, includeFields: Set<HandleNameField>) :
    AbstractCustomDataOperation<HandleNameField, HandleNameEntity>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = HandleNameMimeType

    override fun setCustomData(
        data: HandleNameEntity, setValue: (field: HandleNameField, value: Any?) -> Unit
    ) {
        setValue(HandleNameFields.Handle, data.handle)
    }
}