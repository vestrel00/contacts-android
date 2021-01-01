package contacts.entities.mapper

import contacts.AbstractDataField
import contacts.ContactsField
import contacts.RawContactsField
import contacts.custom.CustomCommonDataRegistry
import contacts.entities.Contact
import contacts.entities.MimeType.*
import contacts.entities.RawContact
import contacts.entities.TempRawContact
import contacts.entities.cursor.*

/**
 * Returns a list of [Contact]s from the given cursor, which assumed to have been retrieved from the
 * Data table.
 */
internal class ContactsMapper(
    /**
     * Support for custom common data.
     */
    private val customDataRegistry: CustomCommonDataRegistry,

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

    val rawContactIds: Set<Long> = rawContactsMap.keys

    val contactIds: Set<Long> = contactsMap.keys

    /**
     * Collects Contacts from the given Contacts table cursor.
     *
     * This will not close the given [cursor].
     */
    fun processContactsCursor(cursor: EntityCursor<ContactsField>): ContactsMapper = apply {
        // Use the Contacts cursor to retrieve the contactId.
        val contactsCursor = cursor.contactsCursor()
        val contactMapper = cursor.contactsMapper()

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
        val tempRawContactMapper = rawContactsCursor.tempRawContactMapper()

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
        val contactMapper = cursor.dataContactsMapper()
        val tempRawContactMapper = dataCursor.tempRawContactMapper()

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
                    .also { cursor.updateRawContact(it, customDataRegistry) }
            }
        }
    }

    /*
     * This used to return a Sequence but I found that it was more CPU and memory intensive.
     * This is especially true if callers of this function call Sequence.count(), which invokes
     * all of the intermediate functions during traversal producing unwanted side effects.
     */
    fun map(): List<Contact> {
        if (cancel()) {
            return emptyList()
        }

        // Map contact id to set of raw contacts.
        val contactRawMap = mutableMapOf<Long, MutableList<RawContact>>()
        for (tempRawContact in rawContactsMap.values) {
            // There shouldn't be any RawContacts that make it here with null contactId.
            if (tempRawContact.contactId == null) continue

            val rawContacts = contactRawMap.getOrPut(tempRawContact.contactId) { mutableListOf() }
            rawContacts.add(tempRawContact.toRawContact())

            // Return empty list if cancelled to ensure only correct data set is returned.
            if (cancel()) {
                return emptyList()
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

            // Return empty list if cancelled to ensure only correct data set is returned.
            if (cancel()) {
                return emptyList()
            }
        }

        // Add all of the remaining RawContacts in contactRawMap without a parent Contact.
        for (entry in contactRawMap) {
            contactList.add(
                Contact(
                    id = entry.key,
                    rawContacts = entry.value.sortedBy { it.id },
                    displayNamePrimary = null,
                    displayNameAlt = null,
                    lastUpdatedTimestamp = null,
                    options = null
                )
            )

            // Return empty list if cancelled to ensure only correct data set is returned.
            if (cancel()) {
                return emptyList()
            }
        }

        return if (cancel()) emptyList() else contactList
    }
}

private fun EntityCursor<AbstractDataField>.updateRawContact(
    rawContact: TempRawContact,
    customDataRegistry: CustomCommonDataRegistry
) {
    // Each row in the cursor only contains a subset of contact data paired by the mime type.
    // This is why full contact objects cannot be built per cursor row.
    // Therefore, mutable contact objects must be updated with different pieces of data
    // that each cursor row provides.
    when (val mimeType = mimeTypeCursor(customDataRegistry).mimeType) {
        Address -> rawContact.addresses.add(addressMapper().value)
        Email -> rawContact.emails.add(emailMapper().value)
        Event -> rawContact.events.add(eventMapper().value)
        GroupMembership -> rawContact.groupMemberships.add(groupMembershipMapper().value)
        Im -> rawContact.ims.add(imMapper().value)
        Name -> rawContact.name = nameMapper().value
        Nickname -> rawContact.nickname = nicknameMapper().value
        Note -> rawContact.note = noteMapper().value
        Organization -> rawContact.organization = organizationMapper().value
        Phone -> rawContact.phones.add(phoneMapper().value)
        Photo -> rawContact.photo = photoMapper().value
        Relation -> rawContact.relations.add(relationMapper().value)
        SipAddress -> rawContact.sipAddress = sipAddressMapper().value
        Website -> rawContact.websites.add(websiteMapper().value)
        is Custom -> {
            val customDataMapper = customDataRegistry
                .customCommonDataMapperFactoryOf(mimeType)
                ?.create(cursor)
                ?: throw IllegalStateException("No custom data mapper found for ${mimeType.value}")

            val customDataList = rawContact.customData.getOrPut(mimeType.value) {
                mutableListOf()
            }
            customDataList.add(customDataMapper.value)
        }
        Unknown -> {
            // Do nothing
        }
    }
}