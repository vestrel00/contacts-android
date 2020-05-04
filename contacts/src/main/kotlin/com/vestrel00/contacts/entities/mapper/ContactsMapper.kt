package com.vestrel00.contacts.entities.mapper

import android.database.Cursor
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MimeType.*
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.entities.TempRawContact
import com.vestrel00.contacts.entities.cursor.*

/**
 * Returns a list of [Contact]s from the given cursor, which assumed to have been retrieved from the
 * Data table.
 */
internal class ContactsMapper(

    /**
     * True if the cursors used to collect contacts contains data belonging to the user profile.
     */
    private val isProfile: Boolean,

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
     * Collects Contacts from the given Contacts table cursor.
     *
     * This will not close the given [cursor].
     */
    fun processContactsCursor(cursor: Cursor): ContactsMapper = apply {
        // Use the Contacts cursor to retrieve the contactId.
        val contactsCursor = cursor.contactsCursor()

        cursor.resetPosition()
        while (!cancel() && cursor.moveToNext()) {
            val contactId = contactsCursor.contactId

            if (contactId != null && !contactsMap.containsKey(contactId)) {
                contactsMap[contactId] =
                    cursor.contactMapper(contactsCursor, isProfile).value
            }
        }
    }

    /**
     * Collects RawContacts from the given RawContacts table cursor.
     *
     * This will not close the given [cursor].
     */
    fun processRawContactsCursor(cursor: Cursor): ContactsMapper = apply {
        // Use the RawContacts cursor to retrieve the rawContactId.
        val rawContactsCursor = cursor.rawContactsCursor()

        cursor.resetPosition()
        while (!cancel() && cursor.moveToNext()) {
            val rawContactId = rawContactsCursor.rawContactId

            if (rawContactId != null && !rawContactsMap.containsKey(rawContactId)) {
                rawContactsMap[rawContactId] =
                    cursor.tempRawContactMapper(rawContactsCursor, isProfile).value
            }
        }
    }

    /**
     * Collects Contacts, RawContacts, and Data from the given Data table cursor.
     *
     * This will not close the given [cursor].
     */
    fun processDataCursor(cursor: Cursor): ContactsMapper = apply {
        // Changing the cursor position also changes the values returned by the mappers.
        val dataCursor = cursor.dataCursor()
        val contactMapper = cursor.contactMapper(dataCursor, isProfile)
        val tempRawContactMapper = cursor.tempRawContactMapper(dataCursor, isProfile)

        cursor.resetPosition()
        while (!cancel() && cursor.moveToNext()) {

            // Collect contacts.
            // Use the Data cursor to retrieve the contactId.
            val contactId = dataCursor.contactId
            if (contactId != null && !contactsMap.containsKey(contactId)) {
                contactsMap[contactId] = contactMapper.value
            }

            // Collect the RawContacts and update them.
            // Use the Data cursor to retrieve the rawContactId.
            val rawContactId = dataCursor.rawContactId
            if (rawContactId != null) {
                rawContactsMap.getOrPut(rawContactId) { tempRawContactMapper.value }.also {
                    cursor.updateRawContact(it)
                }
            }
        }
    }

    fun map(): Sequence<Contact> {
        sortRawContactsDataLists()

        // Map contact id to set of raw contacts.
        val contactRawMap = mutableMapOf<Long, MutableList<RawContact>>()
        for (tempRawContact in rawContactsMap.values) {
            if (tempRawContact.contactId == null) continue

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
            val rawContacts = if (contact.id != null) {
                contactRawMap.getOrElse(contact.id) { emptyList<RawContact>() }
            } else {
                emptyList()
            }

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
            EMAIL -> rawContact.emails.add(emailMapper().value)
            EVENT -> rawContact.events.add(eventMapper().value)
            GROUP_MEMBERSHIP -> rawContact.groupMemberships.add(groupMembershipMapper().value)
            IM -> rawContact.ims.add(imMapper().value)
            NAME -> rawContact.name = nameMapper().value
            NICKNAME -> rawContact.nickname = nicknameMapper().value
            NOTE -> rawContact.note = noteMapper().value
            ORGANIZATION -> rawContact.organization = organizationMapper().value
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

            if (cancel()) {
                break
            }
        }
    }
}