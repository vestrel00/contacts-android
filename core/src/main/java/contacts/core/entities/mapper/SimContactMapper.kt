package contacts.core.entities.mapper

import contacts.core.entities.SimContact
import contacts.core.entities.cursor.SimContactCursor

internal class SimContactMapper(private val simContactCursor: SimContactCursor) :
    EntityMapper<SimContact> {

    override val value: SimContact
        get() = SimContact(
            id = simContactCursor.id,

            name = simContactCursor.name,
            number = simContactCursor.number,
            email = simContactCursor.email,

            isRedacted = false
        )
}
