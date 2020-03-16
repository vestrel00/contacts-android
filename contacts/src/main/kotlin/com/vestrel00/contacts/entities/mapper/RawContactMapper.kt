package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.entities.cursor.RawContactCursor

internal class RawContactMapper(private val rawContactCursor: RawContactCursor) :
    EntityMapper<RawContact, MutableRawContact> {

    override val toImmutable: RawContact
        get() = RawContact(
            id = rawContactCursor.id,
            contactId = rawContactCursor.contactId,

            addresses = emptyList(),

            company = null,

            emails = emptyList(),

            events = emptyList(),

            groupMemberships = emptyList(),

            ims = emptyList(),

            name = null,

            nickname = null,

            note = null,

            phones = emptyList(),

            relations = emptyList(),

            sipAddress = null,

            websites = emptyList()
        )

    override val toMutable: MutableRawContact
        get() = MutableRawContact(
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
