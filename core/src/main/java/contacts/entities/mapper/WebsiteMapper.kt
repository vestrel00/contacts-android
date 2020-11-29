package contacts.entities.mapper

import contacts.entities.Website
import contacts.entities.cursor.WebsiteCursor

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
