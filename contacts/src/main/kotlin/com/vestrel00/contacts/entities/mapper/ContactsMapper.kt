package com.vestrel00.contacts.entities.mapper

import android.database.Cursor
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MimeType.*
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact

/**
 * Returns a list of [Contact]s from the given cursor, which assumed to have been retrieved from the
 * Data table.
 */
internal class ContactsMapper(
    /**
     * A map of contact id to [Contact].
     */
    private val contactsMap: MutableMap<Long, Contact> = mutableMapOf(),

    /**
     * A map of raw contact id to a [MutableRawContact]s.
     */
    private val rawContactsMap: MutableMap<Long, MutableRawContact> = mutableMapOf(),

    private val entityMapper: EntityMapper = EntityMapper()
) {

    /**
     * Returns a sequence of contacts.
     *
     * If [cancel] returns true while the [cursor] is being traversed, an empty sequence will be
     * returned regardless of the cursor data processed before cancellation. This is done to ensure
     * that only correct and complete data set is returned.
     */
    fun fromCursor(cursor: Cursor, cancel: () -> Boolean): Sequence<Contact> {
        entityMapper.init(cursor)

        cursor.moveToPosition(-1)

        // Changing the cursor position also changes the values returned by the entityMapper.
        while (cursor.moveToNext()) {
            // Collect contacts.
            val contactId = entityMapper.contactId
            if (!contactsMap.containsKey(contactId)) {
                contactsMap[contactId] = entityMapper.contact
            }

            // Collect the RawContacts and update them.
            val rawContactId = entityMapper.rawContactId
            val rawContact = rawContactsMap.getOrPut(rawContactId) { entityMapper.rawContact }
            updateRawContact(rawContact)

            if (cancel()) {
                // Return empty sequence if cancelled to ensure only correct data set is returned.
                return emptySequence()
            }
        }
        cursor.close()

        sortRawContactsDataLists()

        // Map contact id to set of raw contacts.
        val contactRawMap = mutableMapOf<Long, MutableList<RawContact>>()
        for (rawContact in rawContactsMap.values) {
            val rawContacts = contactRawMap.getOrPut(rawContact.contactId) { mutableListOf() }
            rawContacts.add(rawContact.toRawContact())

            if (cancel()) {
                // Return empty sequence if cancelled to ensure only correct data set is returned.
                return emptySequence()
            }
        }

        return contactsMap.values.asSequence().map { contact ->
            val rawContacts = contactRawMap.getOrElse(contact.id) { emptyList<RawContact>() }

            // The data class copy function comes in handy here.
            // Sort RawContacts by id as specified by RawContact.id.
            contact.copy(rawContacts = rawContacts.sortedBy { it.id })
        }
    }

    private fun updateRawContact(rawContact: MutableRawContact) {
        // Each row in the cursor only contains a subset of contact data paired by the mime type.
        // This is why full contact objects cannot be built per cursor row.
        // Therefore, mutable contact objects must be updated with different pieces of data
        // that each cursor row provides.
        when (entityMapper.mimeType) {
            ADDRESS -> rawContact.addresses.add(entityMapper.address)
            COMPANY -> rawContact.company = entityMapper.company
            EMAIL -> rawContact.emails.add(entityMapper.email)
            EVENT -> rawContact.events.add(entityMapper.event)
            GROUP_MEMBERSHIP -> rawContact.groupMemberships.add(entityMapper.groupMembership)
            IM -> rawContact.ims.add(entityMapper.im)
            NAME -> rawContact.name = entityMapper.name
            NICKNAME -> rawContact.nickname = entityMapper.nickname
            NOTE -> rawContact.note = entityMapper.note
            PHONE -> rawContact.phones.add(entityMapper.phone)
            RELATION -> rawContact.relations.add(entityMapper.relation)
            SIP_ADDRESS -> rawContact.sipAddress = entityMapper.sipAddress
            WEBSITE -> rawContact.websites.add(entityMapper.website)

            // Photo types are not included as an entity. Photo extension functions exist to get/set
            // Contact and RawContact photos.
            PHOTO, UNKNOWN -> {
                // Do nothing
            }
        }
    }

    private fun sortRawContactsDataLists() {
        for (rawContact in rawContactsMap.values) {
            rawContact.addresses.sortBy { it.formattedAddress }
            rawContact.emails.sortBy { it.address }
            rawContact.events.sortBy { it.date?.time?.toString() }
            rawContact.groupMemberships.sortBy { it.groupId.toString() }
            rawContact.ims.sortBy { it.data }
            rawContact.phones.sortBy { it.normalizedNumber }
            rawContact.relations.sortBy { it.name }
            rawContact.websites.sortBy { it.url }
        }
    }
}