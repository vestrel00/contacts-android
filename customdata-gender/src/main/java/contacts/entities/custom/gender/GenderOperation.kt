package contacts.entities.custom.gender

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class GenderOperationFactory :
    AbstractCustomDataOperation.Factory<GenderField, GenderEntity> {

    override fun create(
        isProfile: Boolean, includeFields: Set<GenderField>
    ): AbstractCustomDataOperation<GenderField, GenderEntity> =
        GenderOperation(isProfile, includeFields)
}

private class GenderOperation(isProfile: Boolean, includeFields: Set<GenderField>) :
    AbstractCustomDataOperation<GenderField, GenderEntity>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = GenderMimeType

    override fun setCustomData(
        data: GenderEntity, setValue: (field: GenderField, value: Any?) -> Unit
    ) {
        setValue(GenderFields.Type, data.type?.value)
        setValue(GenderFields.Label, data.label)
    }
}