package contacts.entities.custom.gender

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class GenderOperationFactory :
    AbstractCustomDataOperation.Factory<GenderField, GenderEntity> {

    override fun create(
        callerIsSyncAdapter: Boolean,
        isProfile: Boolean,
        includeFields: Set<GenderField>?
    ): AbstractCustomDataOperation<GenderField, GenderEntity> = GenderOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        includeFields = includeFields
    )
}

private class GenderOperation(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<GenderField>?
) : AbstractCustomDataOperation<GenderField, GenderEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    override val mimeType: MimeType.Custom = GenderMimeType

    override fun setCustomData(
        data: GenderEntity, setValue: (field: GenderField, value: Any?) -> Unit
    ) {
        setValue(GenderFields.Type, data.type?.value)
        setValue(GenderFields.Label, data.label)
    }
}