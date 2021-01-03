package contacts.entities.operation

import contacts.EventField
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableEvent
import contacts.entities.mapper.EventMapper

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