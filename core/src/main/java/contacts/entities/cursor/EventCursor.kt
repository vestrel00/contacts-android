package contacts.entities.cursor

import android.database.Cursor
import contacts.EventField
import contacts.Fields
import contacts.entities.Event

/**
 * Retrieves [Fields.Event] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class EventCursor(cursor: Cursor) : AbstractDataCursor<EventField>(cursor) {

    val type: Event.Type?
        get() = Event.Type.fromValue(getInt(Fields.Event.Type))

    val label: String?
        get() = getString(Fields.Event.Label)

    val date: String?
        get() = getString(Fields.Event.Date)
}
