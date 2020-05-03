package com.vestrel00.contacts.entities

import kotlinx.android.parcel.Parcelize

// TODO Update all utils and other classes to use this instead of entire RawContact / MutableRawContact classes.
/**
 * [Entity] in the RawContacts table.
 */
interface RawContactEntity : Entity {
    /**
     * The id of the RawContacts row this represents.
     *
     * The value of RawContacts._ID / Data.RAW_CONTACT_ID.
     */
    override val id: Long?

    /**
     * The ID of the [Contact] that this [RawContact] is associated with.
     *
     * The value of RawContacts.CONTACT_ID / Data.CONTACT_ID.
     */
    val contactId: Long?

    /**
     * True if this raw contact belongs to the user's personal profile entry.
     */
    val isProfile: Boolean
}

/**
 * Contact data specific to an [android.accounts.Account].
 *
 * ## Note
 *
 * A [Contact] may consist of one or more [RawContact]. A [RawContact] is an association between a
 * Contact and an [android.accounts.Account]. Each [RawContact] is associated with several pieces of
 * Data such as emails.
 *
 * The Contacts Provider may combine [RawContact] from several different Accounts. The same effect
 * is achieved when merging / linking multiple contacts.
 *
 * ## Developer notes
 *
 * MutableLists are used instead of MutableSets to allow for duplicates, which are allowed in the
 * native Contacts app. Technically, sets could be used here because each data has different row id.
 * However, [MutableRawContact] data all have invalid ids, which disallows duplicates for consumer-
 * created data instances. Therefore, lists are also used here for parity. Besides, lists are more
 * commonly used in Android development and are more supported than sets.
 */
@Parcelize
data class RawContact internal constructor(

    /**
     * See [RawContactEntity.id].
     */
    override val id: Long?,

    /**
     * See [RawContactEntity.contactId].
     */
    override val contactId: Long?,

    /**
     * See [RawContactEntity.isProfile].
     */
    override val isProfile: Boolean,

    /**
     * An immutable list of addresses.
     */
    val addresses: List<Address>,

    val company: Company?,

    /**
     * An immutable list of emails.
     */
    val emails: List<Email>,

    /**
     * An immutable list of events.
     */
    val events: List<Event>,

    /**
     * An immutable list of group memberships.
     */
    val groupMemberships: List<GroupMembership>,

    /**
     * An immutable list of ims.
     */
    val ims: List<Im>,

    val name: Name?,

    val nickname: Nickname?,

    val note: Note?,

    // Use the ContactOptions extension functions to get/set options.
    // The Data table contains the options columns for Contacts, not for RawContacts.

    /**
     * An immutable list of phones.
     */
    val phones: List<Phone>,

    /**
     * An immutable list of relations.
     */
    val relations: List<Relation>,

    val sipAddress: SipAddress?,

    /**
     * An immutable list of websites.
     */
    val websites: List<Website>

) : RawContactEntity {

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        company, name, nickname, note, sipAddress
    ) && entitiesAreAllBlank(
        addresses, emails, events, groupMemberships, ims, phones, relations, websites
    )

    fun toMutableRawContact() = MutableRawContact(
        id = id,
        contactId = contactId,

        isProfile = isProfile,

        addresses = addresses.asSequence().map { it.toMutableAddress() }.toMutableList(),

        company = company?.toMutableCompany(),

        emails = emails.asSequence().map { it.toMutableEmail() }.toMutableList(),

        events = events.asSequence().map { it.toMutableEvent() }.toMutableList(),

        groupMemberships = groupMemberships.toMutableList(),

        ims = ims.asSequence().map { it.toMutableIm() }.toMutableList(),

        name = name?.toMutableName(),

        nickname = nickname?.toMutableNickname(),

        note = note?.toMutableNote(),

        phones = phones.asSequence().map { it.toMutablePhone() }.toMutableList(),

        relations = relations.asSequence().map { it.toMutableRelation() }.toMutableList(),

        sipAddress = sipAddress?.toMutableSipAddress(),

        websites = websites.asSequence().map { it.toMutableWebsite() }.toMutableList()
    )
}

/**
 * A mutable [RawContact].
 */
@Parcelize
data class MutableRawContact internal constructor(

    /**
     * See [RawContact.id].
     */
    override val id: Long?,

    /**
     * See [RawContact.contactId].
     */
    override val contactId: Long?,

    /**
     * See [RawContact.isProfile].
     */
    override val isProfile: Boolean,

    /**
     * Mutable version of [RawContact.addresses].
     */
    var addresses: MutableList<MutableAddress>,

    /**
     * Mutable version of [RawContact.company].
     */
    var company: MutableCompany?,

    /**
     * Mutable version of [RawContact.emails].
     */
    var emails: MutableList<MutableEmail>,

    /**
     * Mutable version of [RawContact.events].
     */
    var events: MutableList<MutableEvent>,

    /**
     * Mutable version of [RawContact.groupMemberships].
     *
     * Only group memberships to groups that belong to the same account as this contact will be
     * inserted. Group membership to the account's default group will not be deleted even if it
     * is removed in this list!
     */
    var groupMemberships: MutableList<GroupMembership>,

    /**
     * Mutable version of [RawContact.ims].
     */
    var ims: MutableList<MutableIm>,

    /**
     * Mutable version of [RawContact.name].
     */
    var name: MutableName?,

    /**
     * Mutable version of [RawContact.nickname].
     */
    var nickname: MutableNickname?,

    /**
     * Mutable version of [RawContact.note].
     */
    var note: MutableNote?,

    // Use the ContactOptions extension functions to get/set options.

    /**
     * Mutable version of [RawContact.phones].
     */
    var phones: MutableList<MutablePhone>,

    /**
     * Mutable version of [RawContact.relations].
     */
    var relations: MutableList<MutableRelation>,

    /**
     * Mutable version of [RawContact.sipAddress].
     */
    var sipAddress: MutableSipAddress?,

    /**
     * Mutable version of [RawContact.websites].
     */
    var websites: MutableList<MutableWebsite>

) : RawContactEntity {

    constructor() : this(
        null, null, false, mutableListOf(), null, mutableListOf(), mutableListOf(),
        mutableListOf(), mutableListOf(), null, null, null, mutableListOf(),
        mutableListOf(), null, mutableListOf()
    )

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        company, name, nickname, note, sipAddress
    ) && entitiesAreAllBlank(
        addresses, emails, events, groupMemberships, ims, phones, relations, websites
    )
}

/**
 * A temporary holder of immutable entities in mutable lists / attribute.
 *
 * Used internally to optimize cursor to contact mappings.
 */
@Parcelize
internal data class TempRawContact constructor(

    override val id: Long?,
    override val contactId: Long?,
    override val isProfile: Boolean,

    var addresses: MutableList<Address>,
    var company: Company?,
    var emails: MutableList<Email>,
    var events: MutableList<Event>,
    var groupMemberships: MutableList<GroupMembership>,
    var ims: MutableList<Im>,
    var name: Name?,
    var nickname: Nickname?,
    var note: Note?,
    var phones: MutableList<Phone>,
    var relations: MutableList<Relation>,
    var sipAddress: SipAddress?,
    var websites: MutableList<Website>

) : RawContactEntity {

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        company, name, nickname, note, sipAddress
    ) && entitiesAreAllBlank(
        addresses, emails, events, groupMemberships, ims, phones, relations, websites
    )

    fun toRawContact() = RawContact(
        id = id,
        contactId = contactId,

        isProfile = isProfile,

        addresses = addresses.toList(),

        company = company,

        emails = emails.toList(),

        events = events.toList(),

        groupMemberships = groupMemberships.toList(),

        ims = ims.toList(),

        name = name,

        nickname = nickname,

        note = note,

        phones = phones.toList(),

        relations = relations.toList(),

        sipAddress = sipAddress,

        websites = websites.toList()
    )
}