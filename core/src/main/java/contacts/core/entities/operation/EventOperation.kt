package contacts.core.entities.operation

import contacts.core.EventField
import contacts.core.Fields
import contacts.core.entities.MimeType
import contacts.core.entities.MutableEvent
import contacts.core.entities.mapper.EventMapper

internal class EventOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<EventField, MutableEvent>(isProfile) {

    override val mimeType = MimeType.Event

    override fun setData(
        data: MutableEvent, setValue: (field: EventField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Event.Type, data.type?.value)
        setValue(Fields.Event.Label, data.label)
        setValue(Fields.Event.Date, EventMapper.dateToString(data.date))
    }
}