package contacts.core.entities.operation

import contacts.core.EventField
import contacts.core.Fields
import contacts.core.entities.EventEntity
import contacts.core.entities.MimeType
import contacts.core.entities.toDbString

internal class EventOperation(
    callerIsSyncAdapter: Boolean, isProfile: Boolean, includeFields: Set<EventField>
) : AbstractDataOperation<EventField, EventEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    override val mimeType = MimeType.Event

    override fun setValuesFromData(
        data: EventEntity, setValue: (field: EventField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Event.Type, data.type?.value)
        setValue(Fields.Event.Label, data.label)
        setValue(Fields.Event.Date, data.date?.toDbString())
    }
}