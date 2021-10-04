package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.EventField
import contacts.core.Fields
import contacts.core.entities.Event

/**
 * Retrieves [Fields.Event] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class EventCursor(cursor: Cursor) : AbstractDataCursor<EventField>(cursor) {

    val type: Event.Type? by type(Fields.Event.Type, typeFromValue = Event.Type::fromValue)

    val label: String? by string(Fields.Event.Label)

    val date: String? by string(Fields.Event.Date)
}
