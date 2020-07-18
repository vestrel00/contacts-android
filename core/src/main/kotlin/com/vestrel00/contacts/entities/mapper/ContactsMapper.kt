package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.AbstractDataField
import com.vestrel00.contacts.ContactsField
import com.vestrel00.contacts.RawContactsField
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
    fun processContactsCursor(cursor: EntityCursor<ContactsField>): ContactsMapper = apply {
        // Use the Contacts cursor to retrieve the contactId.
        val contactsCursor = cursor.contactsCursor()
        val contactMapper = cursor.contactsMapper(isProfile)

        cursor.resetPosition()
        while (!cancel() && cursor.moveToNext()) {
            val contactId = contactsCursor.contactId

            if (contactId != null && !contactsMap.containsKey(contactId)) {
                contactsMap[contactId] = contactMapper.value
            }
        }
    }

    /**
     * Collects RawContacts from the given RawContacts table cursor.
     *
     * This will not close the given [cursor].
     */
    fun processRawContactsCursor(cursor: EntityCursor<RawContactsField>): ContactsMapper = apply {
        // Use the RawContacts cursor to retrieve the rawContactId.
        val rawContactsCursor = cursor.rawContactsCursor()
        val tempRawContactMapper = rawContactsCursor.tempRawContactMapper(isProfile)

        cursor.resetPosition()
        while (!cancel() && cursor.moveToNext()) {
            val rawContactId = rawContactsCursor.rawContactId

            if (rawContactId != null && !rawContactsMap.containsKey(rawContactId)) {
                rawContactsMap[rawContactId] = tempRawContactMapper.value
            }
        }
    }

    /**
     * Collects Contacts, RawContacts, and Data from the given Data table cursor.
     *
     * This will not close the given [cursor].
     */
    fun processDataCursor(cursor: EntityCursor<AbstractDataField>): ContactsMapper = apply {
        // Changing the cursor position also changes the values returned by the mappers.
        val dataCursor = cursor.dataCursor()
        val contactMapper = cursor.dataContactsMapper(isProfile)
        val tempRawContactMapper = dataCursor.tempRawContactMapper(isProfile)

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
            dataCursor.rawContactId?.let { rawContactId ->
                rawContactsMap.getOrPut(rawContactId) { tempRawContactMapper.value }
                    .also(cursor::updateRawContact)
            }
        }
    }

    /*
     * This used to return a Sequence but I found that it was more CPU and memory intensive.
     * This is especially true if callers of this function call Sequence.count(), which invokes
     * all of the intermediate functions during traversal producing unwanted side effects.
     */
    fun map(): List<Contact> {
        // Map contact id to set of raw contacts.
        val contactRawMap = mutableMapOf<Long, MutableList<RawContact>>()
        for (tempRawContact in rawContactsMap.values) {
            // There shouldn't be any RawContacts that make it here with null contactId.
            if (tempRawContact.contactId == null) continue

            val rawContacts = contactRawMap.getOrPut(tempRawContact.contactId) { mutableListOf() }
            rawContacts.add(tempRawContact.toRawContact())

            if (cancel()) {
                // Return empty list if cancelled to ensure only correct data set is returned.
                return mutableListOf()
            }
        }

        val contactList = mutableListOf<Contact>()

        // Add all of the Contacts in the contactsMap.
        for (contact in contactsMap.values) {
            // Make sure to remove the entry in contactRawMap as it is processed.
            val rawContacts: List<RawContact> =
                contact.id?.let(contactRawMap::remove) ?: emptyList()

            // The data class copy function comes in handy here.
            contactList.add(contact.copy(rawContacts = rawContacts.sortedBy { it.id }))

            if (cancel()) {
                break
            }
        }

        // Add all of the remaining RawContacts in contactRawMap without a parent Contact.
        for (entry in contactRawMap) {
            contactList.add(
                Contact(
                    id = entry.key,
                    isProfile = isProfile,
                    rawContacts = entry.value.sortedBy { it.id },
                    displayNamePrimary = null,
                    displayNameAlt = null,
                    lastUpdatedTimestamp = null,
                    options = null
                )
            )

            if (cancel()) {
                break
            }
        }

        return if (cancel()) emptyList() else contactList
    }

}

private fun EntityCursor<AbstractDataField>.updateRawContact(rawContact: TempRawContact) {
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