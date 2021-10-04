package contacts.entities.custom.handlename

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class HandleNameDataOperationFactory : AbstractCustomDataOperation.Factory<HandleNameField,
        MutableHandleName> {

    override fun create(isProfile: Boolean): AbstractCustomDataOperation<HandleNameField,
            MutableHandleName> = HandleNameDataOperation(isProfile)
}

private class HandleNameDataOperation(isProfile: Boolean) :
    AbstractCustomDataOperation<HandleNameField, MutableHandleName>(isProfile) {

    override val mimeType: MimeType.Custom = HandleNameMimeType

    override fun setCustomData(
        data: MutableHandleName, setValue: (field: HandleNameField, value: Any?) -> Unit
    ) {
        setValue(HandleNameFields.Handle, data.handle)
    }
}