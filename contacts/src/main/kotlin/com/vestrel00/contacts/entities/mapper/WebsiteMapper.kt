package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableWebsite
import com.vestrel00.contacts.entities.Website
import com.vestrel00.contacts.entities.cursor.WebsiteCursor

internal class WebsiteMapper(private val websiteCursor: WebsiteCursor) :
    EntityMapper<Website, MutableWebsite> {

    override val toImmutable: Website
        get() = Website(
            id = websiteCursor.id,
            rawContactId = websiteCursor.rawContactId,
            contactId = websiteCursor.contactId,

            isPrimary = websiteCursor.isPrimary,
            isSuperPrimary = websiteCursor.isSuperPrimary,

            url = websiteCursor.url
        )

    override val toMutable: MutableWebsite
        get() = MutableWebsite(
            id = websiteCursor.id,
            rawContactId = websiteCursor.rawContactId,
            contactId = websiteCursor.contactId,

            isPrimary = websiteCursor.isPrimary,
            isSuperPrimary = websiteCursor.isSuperPrimary,

            url = websiteCursor.url
        )
}
