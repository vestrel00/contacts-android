package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.TempRawContact
import com.vestrel00.contacts.entities.cursor.RawContactCursor

internal class TempRawContactMapper(private val rawContactCursor: RawContactCursor) :
    EntityMapper<TempRawContact> {

    override val value: TempRawContact
        get() = TempRawContact(
            id = rawContactCursor.id,
            contactId = rawContactCursor.contactId,

            addresses = mutableListOf(),

            company = null,

            emails = mutableListOf(),

            events = mutableListOf(),

            groupMemberships = mutableListOf(),

            ims = mutableListOf(),

            name = null,

            nickname = null,

            note = null,

            phones = mutableListOf(),

            relations = mutableListOf(),

            sipAddress = null,

            websites = mutableListOf()
        )
}
