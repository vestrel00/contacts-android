package contacts.core.entities.mapper

import contacts.core.entities.Event
import contacts.core.entities.cursor.EventCursor
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

        // FIXME Are these all of the possible date formats? Or are we missing something?
        // Was this coincidence or did the Android team choose these format on purpose? Are these
        // formats influenced by device local and/or OEM modifications? Only time will tell. The
        // community should file bugs if they see any issues. Though, I should probably check this
        // again when we get closer to v1.0.0.
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private val DATE_FORMAT_NO_YEAR = SimpleDateFormat("--MM-dd", Locale.US)

        /**
         * The [dateStr] format is either yyyy-MM-dd (e.g. 2019-08-21) or
         * --MM-dd (if no year, e.g. --08-21).
         *
         * This allows for greater than and less than inequality queries.
         *
         * Note that comparing dates with a year to dates without a year will always result in dates
         * without a year being less than dates with a year. For example, `"--11-11" < "2020-10-10"`
         * is true.
         */
        fun dateFromString(dateStr: String?): Date? = dateStr?.let {
            try {
                // TODO propagate this fix outside of this function. The current Event.date
                // entities do not handle or mention date with no year formats. Perhaps, create a
                // subclass of Date called DateWithNoYear? Or use composition?
                if (dateStr.startsWith("-")) {
                    DATE_FORMAT_NO_YEAR.parse(it)
                } else {
                    DATE_FORMAT.parse(it)
                }
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
