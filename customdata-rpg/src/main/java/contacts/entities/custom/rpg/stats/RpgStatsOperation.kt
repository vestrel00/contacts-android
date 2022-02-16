package contacts.entities.custom.rpg.stats

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation
import contacts.entities.custom.rpg.RpgFields
import contacts.entities.custom.rpg.RpgMimeType
import contacts.entities.custom.rpg.RpgStatsField

internal class RpgStatsOperationFactory :
    AbstractCustomDataOperation.Factory<RpgStatsField, RpgStatsEntity> {

    override fun create(
        isProfile: Boolean, includeFields: Set<RpgStatsField>
    ): AbstractCustomDataOperation<RpgStatsField, RpgStatsEntity> =
        RpgStatsOperation(isProfile, includeFields)
}

private class RpgStatsOperation(isProfile: Boolean, includeFields: Set<RpgStatsField>) :
    AbstractCustomDataOperation<RpgStatsField, RpgStatsEntity>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = RpgMimeType.Stats

    override fun setCustomData(
        data: RpgStatsEntity, setValue: (field: RpgStatsField, value: Any?) -> Unit
    ) {
        setValue(RpgFields.Stats.Level, data.level)
        setValue(RpgFields.Stats.Speed, data.speed)
        setValue(RpgFields.Stats.Strength, data.strength)
        setValue(RpgFields.Stats.Intelligence, data.intelligence)
        setValue(RpgFields.Stats.Luck, data.luck)
    }
}