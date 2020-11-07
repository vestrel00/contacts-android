package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.BlankRawContact
import com.vestrel00.contacts.entities.cursor.RawContactsCursor

/**
 * Creates [BlankRawContact] instances. May be used for cursors from the RawContacts.
 */
internal class BlankRawContactMapper(
    private val rawContactsCursor: RawContactsCursor,
    private val isProfile: Boolean
) : EntityMapper<BlankRawContact> {

    override val value: BlankRawContact
        get() = BlankRawContact(
            id = rawContactsCursor.rawContactId,
            contactId = rawContactsCursor.contactId,
            isProfile = isProfile,

            photo = null,

            displayNamePrimary = rawContactsCursor.displayNamePrimary,
            displayNameAlt = rawContactsCursor.displayNameAlt
        )
}
