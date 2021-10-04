package contacts.core.entities.mapper

import contacts.core.entities.Website
import contacts.core.entities.cursor.WebsiteCursor

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
