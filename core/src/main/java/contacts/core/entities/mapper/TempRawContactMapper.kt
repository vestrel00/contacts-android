package contacts.core.entities.mapper

import contacts.core.entities.TempRawContact
import contacts.core.entities.cursor.RawContactIdCursor

/**
 * Creates [TempRawContact] instances. May be used for cursors from the RawContacts or Data table.
 */
internal class TempRawContactMapper(
    private val rawContactIdCursor: RawContactIdCursor
) : EntityMapper<TempRawContact> {

    override val value: TempRawContact
        get() = TempRawContact(
            id = rawContactIdCursor.rawContactId,
            contactId = rawContactIdCursor.contactId,

            addresses = mutableListOf(),

            emails = mutableListOf(),

            events = mutableListOf(),

            groupMemberships = mutableListOf(),

            ims = mutableListOf(),

            name = null,

            nickname = null,

            note = null,

            organization = null,

            phones = mutableListOf(),

            photo = null,

            relations = mutableListOf(),

            sipAddress = null,

            websites = mutableListOf(),

            customDataEntities = mutableMapOf()
        )
}
