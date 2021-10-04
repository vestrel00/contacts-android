package contacts.core.entities.mapper

import contacts.core.entities.Contact
import contacts.core.entities.Options
import contacts.core.entities.cursor.JoinedContactsCursor

/**
 * Creates [Contact] instances. May be used for cursors from the Contacts or Data table.
 */
internal class ContactMapper(
    private val contactsCursor: JoinedContactsCursor,
    private val optionsMapper: EntityMapper<Options>
) : EntityMapper<Contact> {

    override val value: Contact
        get() = Contact(
            id = contactsCursor.contactId,

            rawContacts = emptyList(),

            // These fields are accessible in both the Contacts and Data tables.
            displayNamePrimary = contactsCursor.displayNamePrimary,
            displayNameAlt = contactsCursor.displayNameAlt,
            lastUpdatedTimestamp = contactsCursor.lastUpdatedTimestamp,

            options = optionsMapper.value
        )
}