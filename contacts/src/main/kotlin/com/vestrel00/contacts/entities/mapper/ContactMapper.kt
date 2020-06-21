package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.Options
import com.vestrel00.contacts.entities.cursor.ContactCursor
import com.vestrel00.contacts.entities.cursor.ContactIdCursor

/**
 * Creates [Contact] instances. May be used for cursors from the Contacts or Data table.
 */
internal class ContactMapper(
    private val contactCursor: ContactCursor,
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
            displayNamePrimary = contactCursor.displayNamePrimary,
            lastUpdatedTimestamp = contactCursor.lastUpdatedTimestamp,

            options = optionsMapper.value
        )
}