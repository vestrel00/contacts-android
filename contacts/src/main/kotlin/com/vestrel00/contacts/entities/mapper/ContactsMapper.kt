package com.vestrel00.contacts.entities.mapper

import android.database.Cursor
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MimeType.*
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.entities.TempRawContact

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
     * A map of raw contact ids to [TempRawContact]s.
     */
    private val rawContactsMap: MutableMap<Long, TempRawContact> = mutableMapOf(),

    private val entityMappers: EntityMapperFactory = EntityMapperFactory()
) {

    /**
     * Returns a sequence of contacts.
     *
     * If [cancel] returns true while the [cursor] is being traversed, an empty sequence will be
     * returned regardless of the cursor data processed before cancellation. This is done to ensure
     * that only correct and complete data set is returned.
     */
    fun fromCursor(cursor: Cursor, cancel: () -> Boolean): Sequence<Contact> {
        entityMappers.init(cursor)

        cursor.moveToPosition(-1)

        // Changing the cursor position also changes the values returned by the entityMapper.
        while (cursor.moveToNext()) {
            // Collect contacts.
            val contactId = entityMappers.contactId
            if (!contactsMap.containsKey(contactId)) {
                contactsMap[contactId] = entityMappers.contactMapper.value
            }

            // Collect the RawContacts and update them.
            val rawContactId = entityMappers.rawContactId
            val rawContact =
                rawContactsMap.getOrPut(rawContactId) { entityMappers.tempRawContactMapper.value }
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
        for (tempRawContact in rawContactsMap.values) {
            val rawContacts = contactRawMap.getOrPut(tempRawContact.contactId) {
                mutableListOf()
            }
            rawContacts.add(tempRawContact.toRawContact())

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

    private fun updateRawContact(rawContact: TempRawContact) {
        // Each row in the cursor only contains a subset of contact data paired by the mime type.
        // This is why full contact objects cannot be built per cursor row.
        // Therefore, mutable contact objects must be updated with different pieces of data
        // that each cursor row provides.
        when (entityMappers.mimeType) {
            ADDRESS -> rawContact.addresses.add(entityMappers.addressMapper.value)
            COMPANY -> rawContact.company = entityMappers.companyMapper.value
            EMAIL -> rawContact.emails.add(entityMappers.emailMapper.value)
            EVENT -> rawContact.events.add(entityMappers.eventMapper.value)
            GROUP_MEMBERSHIP -> rawContact.groupMemberships
                .add(entityMappers.groupMembershipMapper.value)
            IM -> rawContact.ims.add(entityMappers.imMapper.value)
            NAME -> rawContact.name = entityMappers.nameMapper.value
            NICKNAME -> rawContact.nickname = entityMappers.nicknameMapper.value
            NOTE -> rawContact.note = entityMappers.noteMapper.value
            PHONE -> rawContact.phones.add(entityMappers.phoneMapper.value)
            RELATION -> rawContact.relations.add(entityMappers.relationMapper.value)
            SIP_ADDRESS -> rawContact.sipAddress = entityMappers.sipAddressMapper.value
            WEBSITE -> rawContact.websites.add(entityMappers.websiteMapper.value)

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