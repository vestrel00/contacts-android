package contacts.core.entities.mapper

import contacts.core.AbstractDataField
import contacts.core.ContactsField
import contacts.core.RawContactsField
import contacts.core.entities.Contact
import contacts.core.entities.ImmutableCustomDataEntity
import contacts.core.entities.MimeType.Address
import contacts.core.entities.MimeType.Custom
import contacts.core.entities.MimeType.Email
import contacts.core.entities.MimeType.Event
import contacts.core.entities.MimeType.GroupMembership
import contacts.core.entities.MimeType.Im
import contacts.core.entities.MimeType.Name
import contacts.core.entities.MimeType.Nickname
import contacts.core.entities.MimeType.Note
import contacts.core.entities.MimeType.Organization
import contacts.core.entities.MimeType.Phone
import contacts.core.entities.MimeType.Photo
import contacts.core.entities.MimeType.Relation
import contacts.core.entities.MimeType.SipAddress
import contacts.core.entities.MimeType.Unknown
import contacts.core.entities.MimeType.Website
import contacts.core.entities.RawContact
import contacts.core.entities.TempRawContact
import contacts.core.entities.cursor.CursorHolder
import contacts.core.entities.cursor.contactsCursor
import contacts.core.entities.cursor.dataCursor
import contacts.core.entities.cursor.mimeTypeCursor
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.custom.ImmutableCustomDataEntityHolder
import contacts.core.intersect

/**
 * Contains functions that processes cursors from the Contacts ([processContactsCursor]),
 * RawContacts ([processRawContactsCursor]), and Data ([processDataCursor]) tables to accumulate
 * Contacts, RawContacts, and Data.
 *
 * Once accumulation is complete, the [mapContacts] and [mapRawContacts] function returns a list of
 * the accumulated [Contact]s and [RawContact]s respectively.
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

    // We could technically use the assignment operator here because it will assign a reference to
    // the mutable set itself. Meaning the value this returns is not static. However, using get()
    // reads better (less prone to confusion) and is safer.
    val contactIds: Set<Long>
        get() = contactsMap.keys.toSet() // provide an immutable copy for safety

    val rawContactIds: Set<Long>
        get() = rawContactsMap.keys.toSet()

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

            if (!contactsMap.containsKey(contactId)) {
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
        val tempRawContactMapper = cursor.tempRawContactMapper()

        cursor.resetPosition()
        while (!cancel() && cursor.moveToNext()) {
            val rawContactId = rawContactsCursor.rawContactId

            if (!rawContactsMap.containsKey(rawContactId)) {
                rawContactsMap[rawContactId] = tempRawContactMapper.value
            }
        }
    }

    /**
     * Collects Data from the given Data table cursor and updates collected RawContacts.
     *
     * This should only be invoked after [processContactsCursor] and [processRawContactsCursor]
     * have been invoked.
     *
     * This will not close the given [cursor].
     */
    fun processDataCursor(cursor: CursorHolder<AbstractDataField>): ContactsMapper = apply {
        // Changing the cursor position also changes the values returned by the mappers.
        val dataCursor = cursor.dataCursor()

        cursor.resetPosition()
        while (!cancel() && cursor.moveToNext()) {
            rawContactsMap[dataCursor.rawContactId]?.let { tempRawContact ->
                cursor.updateRawContact(customDataRegistry, tempRawContact)
            }
        }
    }

    fun mapContacts(): List<Contact> {
        if (cancel()) {
            return emptyList()
        }

        // Map contact id to set of raw contacts.
        val contactRawMap = mutableMapOf<Long, MutableList<RawContact>>()
        for (tempRawContact in rawContactsMap.values) {

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
            val rawContacts: List<RawContact> = contactRawMap.remove(contact.id) ?: emptyList()

            // The data class copy function comes in handy here.
            contactList.add(contact.copy(rawContacts = rawContacts.sortedBy { it.id }))

            // Return empty list if cancelled to ensure only correct data set is returned.
            if (cancel()) {
                return emptyList()
            }
        }

        return if (cancel()) emptyList() else contactList
    }

    fun mapRawContacts(): List<RawContact> = buildList {
        // In order to support cancellations, we do not just do;
        // rawContactsMap.values.map { it.toRawContact() }
        for (tempRawContact in rawContactsMap.values) {
            add(tempRawContact.toRawContact())
            if (cancel()) {
                return emptyList()
            }
        }
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

        Im -> @Suppress("Deprecation") imMapper().nonBlankValueOrNull?.let(rawContact.ims::add)
        Name -> nameMapper().nonBlankValueOrNull?.let { rawContact.name = it }
        Nickname -> nicknameMapper().nonBlankValueOrNull?.let { rawContact.nickname = it }
        Note -> noteMapper().nonBlankValueOrNull?.let { rawContact.note = it }
        Organization -> organizationMapper().nonBlankValueOrNull?.let {
            rawContact.organization = it
        }

        Phone -> phoneMapper().nonBlankValueOrNull?.let(rawContact.phones::add)
        Photo -> photoMapper().nonBlankValueOrNull?.let { rawContact.photo = it }
        Relation -> relationMapper().nonBlankValueOrNull?.let(rawContact.relations::add)
        SipAddress -> @Suppress("Deprecation") sipAddressMapper().nonBlankValueOrNull?.let {
            rawContact.sipAddress = it
        }

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
        ImmutableCustomDataEntityHolder(
            mutableListOf(),
            customDataEntry.countRestriction,
            rawContact.isRedacted
        )
    }

    val customDataMapper = customDataEntry.mapperFactory.create(
        cursor,
        // Only include custom data fields assigned by this entry.
        includeFields?.let(customDataEntry.fieldSet::intersect)
    )

    // Do not add blanks.
    customDataMapper.nonBlankValueOrNull?.let {
        // We are assuming that the mappers return immutable entities.
        // This is not a safe cast because there can be existing but mutable entities.
        // We could create an "ExistingImmutable" type but we already have so many types.
        // Programming error should be caught very quickly if this implicit contract is violated.
        customDataEntityHolder.entities.add(it as ImmutableCustomDataEntity)
    }
}