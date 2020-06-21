package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.BlankRawContact
import com.vestrel00.contacts.entities.cursor.RawContactIdCursor
import com.vestrel00.contacts.entities.cursor.RawContactsCursor

/**
 * Creates [BlankRawContact] instances. May be used for cursors from the RawContacts or Data table.
 */
internal class BlankRawContactMapper(
    private val rawContactsCursor: RawContactsCursor,
    private val rawContactIdCursor: RawContactIdCursor,
    private val isProfile: Boolean
) : EntityMapper<BlankRawContact> {

    override val value: BlankRawContact
        get() = BlankRawContact(
            id = rawContactIdCursor.rawContactId,
            contactId = rawContactIdCursor.contactId,
            isProfile = isProfile,

            displayNamePrimary = rawContactsCursor.displayNamePrimary,
            displayNameAlt = rawContactsCursor.displayNameAlt
        )
}
