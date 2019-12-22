package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.cursor.RawContactCursor

internal class RawContactMapper(private val rawContactCursor: RawContactCursor) {

    val rawContact: MutableRawContact
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
