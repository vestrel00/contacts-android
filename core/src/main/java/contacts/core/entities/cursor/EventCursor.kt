package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.EventField
import contacts.core.Fields
import contacts.core.entities.EventEntity

/**
 * Retrieves [Fields.Event] data from the given [cursor].
 */
internal class EventCursor(cursor: Cursor, includeFields: Set<EventField>?) :
    AbstractDataCursor<EventField>(cursor, includeFields) {

    val type: EventEntity.Type? by type(
        Fields.Event.Type,
        typeFromValue = EventEntity.Type::fromValue
    )

    val label: String? by string(Fields.Event.Label)

    val date: String? by string(Fields.Event.Date)
}
