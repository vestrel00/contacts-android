package contacts.entities.custom.gender

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class GenderOperationFactory :
    AbstractCustomDataOperation.Factory<GenderField, MutableGender> {

    override fun create(
        isProfile: Boolean, includeFields: Set<GenderField>
    ): AbstractCustomDataOperation<GenderField, MutableGender> =
        GenderOperation(isProfile, includeFields)
}

private class GenderOperation(isProfile: Boolean, includeFields: Set<GenderField>) :
    AbstractCustomDataOperation<GenderField, MutableGender>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = GenderMimeType

    override fun setCustomData(
        data: MutableGender, setValue: (field: GenderField, value: Any?) -> Unit
    ) {
        setValue(GenderFields.Type, data.type?.value)
        setValue(GenderFields.Label, data.label)
    }
}