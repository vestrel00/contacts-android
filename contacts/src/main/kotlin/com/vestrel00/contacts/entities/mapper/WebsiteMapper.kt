package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableWebsite
import com.vestrel00.contacts.entities.cursor.WebsiteCursor

internal class WebsiteMapper(private val websiteCursor: WebsiteCursor) {

    val website: MutableWebsite
        get() = MutableWebsite(
            id = websiteCursor.id,
            rawContactId = websiteCursor.rawContactId,
            contactId = websiteCursor.contactId,

            url = websiteCursor.url
        )
}
