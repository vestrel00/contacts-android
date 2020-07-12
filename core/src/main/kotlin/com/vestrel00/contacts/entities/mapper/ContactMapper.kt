package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.Options
import com.vestrel00.contacts.entities.cursor.ContactIdCursor
import com.vestrel00.contacts.entities.cursor.ContactsCursor

/**
 * Creates [Contact] instances. May be used for cursors from the Contacts or Data table.
 */
internal class ContactMapper(
    private val contactsCursor: ContactsCursor,
    private val contactIdCursor: ContactIdCursor,
    private val optionsMapper: EntityMapper<Options>,
    private val isProfile: Boolean
) : EntityMapper<Contact> {

    override val value: Contact
        get() = Contact(
            id = contactIdCursor.contactId,

            isProfile = isProfile,

            rawContacts = emptyList(),

            // These fields are accessible in both the Contacts and Data tables.
            displayNamePrimary = contactsCursor.displayNamePrimary,
            displayNameAlt = contactsCursor.displayNameAlt,
            lastUpdatedTimestamp = contactsCursor.lastUpdatedTimestamp,

            options = optionsMapper.value
        )
}