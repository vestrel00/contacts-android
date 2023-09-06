package contacts.entities.custom.handlename

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class HandleNameOperationFactory :
    AbstractCustomDataOperation.Factory<HandleNameField, HandleNameEntity> {

    override fun create(
        callerIsSyncAdapter: Boolean, isProfile: Boolean, includeFields: Set<HandleNameField>
    ): AbstractCustomDataOperation<HandleNameField, HandleNameEntity> = HandleNameOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        includeFields = includeFields
    )
}

private class HandleNameOperation(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<HandleNameField>
) : AbstractCustomDataOperation<HandleNameField, HandleNameEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    override val mimeType: MimeType.Custom = HandleNameMimeType

    override fun setCustomData(
        data: HandleNameEntity, setValue: (field: HandleNameField, value: Any?) -> Unit
    ) {
        setValue(HandleNameFields.Handle, data.handle)
    }
}