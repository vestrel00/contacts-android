package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.TempRawContact
import com.vestrel00.contacts.entities.cursor.RawContactIdCursor

/**
 * Creates [TempRawContact] instances. May be used for cursors from the RawContacts or Data table.
 */
internal class TempRawContactMapper(
    private val rawContactIdCursor: RawContactIdCursor,
    private val isProfile: Boolean
) : EntityMapper<TempRawContact> {

    override val value: TempRawContact
        get() = TempRawContact(
            id = rawContactIdCursor.rawContactId,
            contactId = rawContactIdCursor.contactId,

            isProfile = isProfile,

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

            websites = mutableListOf()
        )
}
