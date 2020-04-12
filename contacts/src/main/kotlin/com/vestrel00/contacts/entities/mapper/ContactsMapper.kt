package com.vestrel00.contacts.entities.mapper

import android.database.Cursor
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MimeType.*
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.entities.TempRawContact
import com.vestrel00.contacts.entities.cursor.contactCursor
import com.vestrel00.contacts.entities.cursor.mimeTypeCursor
import com.vestrel00.contacts.entities.cursor.rawContactCursor

/**
 * Returns a list of [Contact]s from the given cursor, which assumed to have been retrieved from the
 * Data table.
 */
internal class ContactsMapper(

    /**
     * If this function returns true while contacts are being looked-up / processed, an empty
     * sequence will be returned regardless of the accumulated data before cancellation. This is
     * done to ensure that only correct and complete data set is returned.
     */
    private val cancel: () -> Boolean,

    /**
     * A map of contact id to [Contact].
     */
    private val contactsMap: MutableMap<Long, Contact> = mutableMapOf(),

    /**
     * A map of raw contact ids to [TempRawContact]s.
     */
    private val rawContactsMap: MutableMap<Long, TempRawContact> = mutableMapOf()
) {

    /**
     * Retrieves contact data from the given Data table cursor.
     *
     * This will not close the given [cursor].
     */
    fun fromCursor(cursor: Cursor): ContactsMapper = apply {
        cursor.moveToPosition(-1)

        // Changing the cursor position also changes the values returned by the entityMapper.
        while (cursor.moveToNext()) {
            // Collect contacts.
            val contactId = cursor.contactCursor().id
            if (!contactsMap.containsKey(contactId)) {
                contactsMap[contactId] = cursor.contactMapper().value
            }

            // Collect the RawContacts and update them.
            val rawContactId = cursor.rawContactCursor().id
            val rawContact =
                rawContactsMap.getOrPut(rawContactId) { cursor.tempRawContactMapper().value }
            cursor.updateRawContact(rawContact)

            if (cancel()) {
                break
            }
        }
    }

    fun map(): Sequence<Contact> {
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

    private fun Cursor.updateRawContact(rawContact: TempRawContact) {
        // Each row in the cursor only contains a subset of contact data paired by the mime type.
        // This is why full contact objects cannot be built per cursor row.
        // Therefore, mutable contact objects must be updated with different pieces of data
        // that each cursor row provides.
        when (mimeTypeCursor().mimeType) {
            ADDRESS -> rawContact.addresses.add(addressMapper().value)
            COMPANY -> rawContact.company = companyMapper().value
            EMAIL -> rawContact.emails.add(emailMapper().value)
            EVENT -> rawContact.events.add(eventMapper().value)
            GROUP_MEMBERSHIP -> rawContact.groupMemberships.add(groupMembershipMapper().value)
            IM -> rawContact.ims.add(imMapper().value)
            NAME -> rawContact.name = nameMapper().value
            NICKNAME -> rawContact.nickname = nicknameMapper().value
            NOTE -> rawContact.note = noteMapper().value
            PHONE -> rawContact.phones.add(phoneMapper().value)
            RELATION -> rawContact.relations.add(relationMapper().value)
            SIP_ADDRESS -> rawContact.sipAddress = sipAddressMapper().value
            WEBSITE -> rawContact.websites.add(websiteMapper().value)

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