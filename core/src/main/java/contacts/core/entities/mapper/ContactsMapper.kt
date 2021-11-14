package contacts.core.entities.mapper

import contacts.core.AbstractDataField
import contacts.core.ContactsField
import contacts.core.RawContactsField
import contacts.core.entities.Contact
import contacts.core.entities.MimeType.*
import contacts.core.entities.RawContact
import contacts.core.entities.TempRawContact
import contacts.core.entities.cursor.*
import contacts.core.entities.custom.CustomDataEntityHolder
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.intersect

/**
 * Contains functions that processes cursors from the Contacts ([processContactsCursor]),
 * RawContacts ([processRawContactsCursor]), and Data ([processDataCursor]) tables to  accumulate
 * Contacts, RawContacts, and Data. Once accumulation is complete, the [map] function returns a list
 * of [Contact]s.
 */
internal class ContactsMapper(
    /**
     * Support for custom common data.
     */
    private val customDataRegistry: CustomDataRegistry,

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
    fun processContactsCursor(cursor: CursorHolder<ContactsField>): ContactsMapper = apply {
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
    fun processRawContactsCursor(cursor: CursorHolder<RawContactsField>): ContactsMapper = apply {
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
    fun processDataCursor(cursor: CursorHolder<AbstractDataField>): ContactsMapper = apply {
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
                    .also { cursor.updateRawContact(customDataRegistry, it) }
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
                    options = null,
                    photoUri = null,
                    photoThumbnailUri = null,
                    hasPhoneNumber = null
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

private fun CursorHolder<AbstractDataField>.updateRawContact(
    customDataRegistry: CustomDataRegistry,
    rawContact: TempRawContact
) {
    // Each row in the cursor only contains a subset of contact data paired by the mime type.
    // This is why full contact objects cannot be built per cursor row. Therefore, mutable contact
    // instances must be updated with different pieces of data that each cursor row provides.
    // Do not add blank **data** as it is just noise.
    when (val mimeType = mimeTypeCursor(customDataRegistry).mimeType) {
        Address -> addressMapper().nonBlankValueOrNull?.let(rawContact.addresses::add)
        Email -> emailMapper().nonBlankValueOrNull?.let(rawContact.emails::add)
        Event -> eventMapper().nonBlankValueOrNull?.let(rawContact.events::add)
        GroupMembership ->
            groupMembershipMapper().nonBlankValueOrNull?.let(rawContact.groupMemberships::add)
        Im -> imMapper().nonBlankValueOrNull?.let(rawContact.ims::add)
        Name -> nameMapper().nonBlankValueOrNull?.let { rawContact.name = it }
        Nickname -> nicknameMapper().nonBlankValueOrNull?.let { rawContact.nickname = it }
        Note -> noteMapper().nonBlankValueOrNull?.let { rawContact.note = it }
        Organization -> organizationMapper().nonBlankValueOrNull?.let {
            rawContact.organization = it
        }
        Phone -> phoneMapper().nonBlankValueOrNull?.let(rawContact.phones::add)
        Photo -> photoMapper().nonBlankValueOrNull?.let { rawContact.photo = it }
        Relation -> relationMapper().nonBlankValueOrNull?.let(rawContact.relations::add)
        SipAddress -> sipAddressMapper().nonBlankValueOrNull?.let { rawContact.sipAddress = it }
        Website -> websiteMapper().nonBlankValueOrNull?.let(rawContact.websites::add)
        is Custom -> updateRawContactCustomData(customDataRegistry, rawContact, mimeType)
        Unknown -> {
            // Do nothing
        }
    }
}

private fun CursorHolder<AbstractDataField>.updateRawContactCustomData(
    customDataRegistry: CustomDataRegistry,
    rawContact: TempRawContact,
    mimeType: Custom
) {
    val customDataEntry = customDataRegistry.entryOf(mimeType)

    val customDataEntityHolder = rawContact.customDataEntities.getOrPut(mimeType.value) {
        CustomDataEntityHolder(mutableListOf(), customDataEntry.countRestriction)
    }

    @Suppress("UNCHECKED_CAST")
    val customDataMapper = customDataEntry.mapperFactory.create(
        cursor,
        // Only include custom data fields assigned by this entry.
        customDataEntry.fieldSet.intersect(includeFields)
    )

    // Do not add blanks.
    customDataMapper.nonBlankValueOrNull?.let(customDataEntityHolder.entities::add)
}