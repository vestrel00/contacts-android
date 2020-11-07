package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Website
import com.vestrel00.contacts.entities.cursor.WebsiteCursor

internal class WebsiteMapper(private val websiteCursor: WebsiteCursor) : EntityMapper<Website> {

    override val value: Website
        get() = Website(
            id = websiteCursor.dataId,
            rawContactId = websiteCursor.rawContactId,
            contactId = websiteCursor.contactId,

            isPrimary = websiteCursor.isPrimary,
            isSuperPrimary = websiteCursor.isSuperPrimary,

            url = websiteCursor.url
        )
}
