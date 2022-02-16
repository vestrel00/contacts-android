package contacts.entities.custom.rpg.profession

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation
import contacts.entities.custom.rpg.RpgFields
import contacts.entities.custom.rpg.RpgMimeType
import contacts.entities.custom.rpg.RpgProfessionField

internal class RpgProfessionOperationFactory :
    AbstractCustomDataOperation.Factory<RpgProfessionField, RpgProfessionEntity> {

    override fun create(
        isProfile: Boolean, includeFields: Set<RpgProfessionField>
    ): AbstractCustomDataOperation<RpgProfessionField, RpgProfessionEntity> =
        RpgProfessionOperation(isProfile, includeFields)
}

private class RpgProfessionOperation(isProfile: Boolean, includeFields: Set<RpgProfessionField>) :
    AbstractCustomDataOperation<RpgProfessionField, RpgProfessionEntity>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = RpgMimeType.Profession

    override fun setCustomData(
        data: RpgProfessionEntity, setValue: (field: RpgProfessionField, value: Any?) -> Unit
    ) {
        setValue(RpgFields.Profession.Title, data.title)
    }
}