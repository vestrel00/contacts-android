package contacts.entities.custom.gender

import contacts.entities.MimeType
import contacts.entities.custom.AbstractCustomDataOperation

internal class GenderDataOperationFactory : AbstractCustomDataOperation.Factory<GenderField,
        MutableGender> {

    override fun create(isProfile: Boolean): AbstractCustomDataOperation<GenderField,
            MutableGender> = GenderDataOperation(isProfile)
}

private class GenderDataOperation(isProfile: Boolean) :
    AbstractCustomDataOperation<GenderField, MutableGender>(isProfile) {

    override val mimeType: MimeType.Custom = GenderMimeType

    override fun setCustomData(
        data: MutableGender, setValue: (field: GenderField, value: Any?) -> Unit
    ) {
        setValue(GenderFields.Type, data.type?.value)
        setValue(GenderFields.Label, data.label)
    }
}