package contacts.core.entities.operation

import contacts.core.EventField
import contacts.core.Fields
import contacts.core.entities.MimeType
import contacts.core.entities.MutableEvent
import contacts.core.entities.toDbString

internal class EventOperation(isProfile: Boolean, includeFields: Set<EventField>) :
    AbstractCommonDataOperation<EventField, MutableEvent>(isProfile, includeFields) {

    override val mimeType = MimeType.Event

    override fun setData(
        data: MutableEvent, setValue: (field: EventField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Event.Type, data.type?.value)
        setValue(Fields.Event.Label, data.label)
        setValue(Fields.Event.Date, data.date?.toDbString())
    }
}