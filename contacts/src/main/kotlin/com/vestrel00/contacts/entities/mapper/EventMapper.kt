package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Event
import com.vestrel00.contacts.entities.cursor.EventCursor
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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

            date = dateFromString(eventCursor.date)
        )

    companion object {

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        /**
         * The [dateStr] format is yyyy-MM-dd. E.G. 2019-08-21. This allows for greater than
         * and less than inequality queries! Was this coincidence or did the Android team choose
         * this format on purpose =)
         */
        fun dateFromString(dateStr: String?): Date? = dateStr?.let {
            try {
                DATE_FORMAT.parse(it)
            } catch (pe: ParseException) {
                null
            }
        }

        /**
         * Outputs the [date] as a string with format [DATE_FORMAT].
         */
        fun dateToString(date: Date?): String? = date?.let(DATE_FORMAT::format)
    }
}
