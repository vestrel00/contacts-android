package contacts.core.entities.mapper

import contacts.core.entities.BlankRawContact
import contacts.core.entities.cursor.RawContactsCursor

/**
 * Creates [BlankRawContact] instances. May be used for cursors from the RawContacts.
 */
internal class BlankRawContactMapper(
    private val rawContactsCursor: RawContactsCursor
) : EntityMapper<BlankRawContact> {

    override val value: BlankRawContact
        get() = BlankRawContact(
            id = rawContactsCursor.rawContactId,
            contactId = rawContactsCursor.contactId,

            photo = null,

            displayNamePrimary = rawContactsCursor.displayNamePrimary,
            displayNameAlt = rawContactsCursor.displayNameAlt
        )
}
