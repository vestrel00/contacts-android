package contacts.core.entities.mapper

import contacts.core.entities.Event
import contacts.core.entities.EventDate
import contacts.core.entities.cursor.EventCursor

internal class EventMapper(private val eventCursor: EventCursor) : EntityMapper<Event> {

    override val value: Event
        get() = Event(
            id = eventCursor.dataId,
            rawContactId = eventCursor.rawContactId,
            contactId = eventCursor.contactId,

            isPrimary = eventCursor.isPrimary,
            isSuperPrimary = eventCursor.isSuperPrimary,

            type = eventCursor.type,
            label = eventCursor.label,

            date = EventDate.fromDateStrFromDb(eventCursor.date)
        )
}
