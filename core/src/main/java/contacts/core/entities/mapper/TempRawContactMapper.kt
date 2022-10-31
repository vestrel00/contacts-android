package contacts.core.entities.mapper

import contacts.core.entities.Options
import contacts.core.entities.TempRawContact
import contacts.core.entities.cursor.RawContactsCursor
import contacts.core.entities.cursor.account

/**
 * Creates [TempRawContact] instances. May be used for cursors from the RawContacts or Data table.
 */
internal class TempRawContactMapper(
    private val rawContactsCursor: RawContactsCursor,
    private val optionsMapper: EntityMapper<Options>
) : EntityMapper<TempRawContact> {

    override val value: TempRawContact
        get() = TempRawContact(
            id = rawContactsCursor.rawContactId,
            contactId = rawContactsCursor.contactId,

            displayNamePrimary = rawContactsCursor.displayNamePrimary,
            displayNameAlt = rawContactsCursor.displayNameAlt,
            account = rawContactsCursor.account(),

            addresses = mutableListOf(),

            emails = mutableListOf(),

            events = mutableListOf(),

            groupMemberships = mutableListOf(),

            ims = mutableListOf(),

            name = null,

            nickname = null,

            note = null,

            options = optionsMapper.value,

            organization = null,

            phones = mutableListOf(),

            photo = null,

            relations = mutableListOf(),

            sipAddress = null,

            websites = mutableListOf(),

            customDataEntities = mutableMapOf(),

            isRedacted = false
        )
}
