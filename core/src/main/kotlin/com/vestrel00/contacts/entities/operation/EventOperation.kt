package com.vestrel00.contacts.entities.operation

import com.vestrel00.contacts.Field
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableEvent
import com.vestrel00.contacts.entities.mapper.EventMapper

internal class EventOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableEvent>(isProfile) {

    override val mimeType = MimeType.EVENT

    override fun setData(
        data: MutableEvent, setValue: (field: Field, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Event.Type, data.type?.value)
        setValue(Fields.Event.Label, data.label)
        setValue(Fields.Event.Date, EventMapper.dateToString(data.date))
    }
}