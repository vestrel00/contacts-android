package contacts.core.entities

import contacts.core.entities.custom.CustomDataEntityHolder
import contacts.core.util.isProfileId
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * [Entity] in the RawContacts table.
 */
// See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
sealed interface RawContactEntity : Entity {
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
        get() = id.isProfileId
}

/**
 * Contains contact data that belong to an [android.accounts.Account].
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
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
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

    // The Data table contains the display name for Contacts, not for RawContacts.

    /**
     * An immutable list of addresses.
     */
    val addresses: List<Address>,

    /**
     * An immutable list of emails.
     */
    val emails: List<Email>,

    /**
     * An immutable list of events.
     *
     * If this raw contact is not associated with an Account, then this will be ignored during
     * inserts and updates.
     */
    val events: List<Event>,

    /**
     * An immutable list of group memberships.
     *
     * If this raw contact is not associated with an Account, then this will be ignored during
     * inserts and updates.
     */
    val groupMemberships: List<GroupMembership>,

    /**
     * An immutable list of ims.
     */
    val ims: List<Im>,

    val name: Name?,

    val nickname: Nickname?,

    val note: Note?,

    // Use the RawContactOptions extension functions to get/set options.
    // The Data table contains the options columns for Contacts, not for RawContacts.

    val organization: Organization?,

    /**
     * An immutable list of phones.
     */
    val phones: List<Phone>,

    /**
     * The [Photo] class does not have any real functional value. This exist only to prevent
     * RawContacts from being considered blanks, which may result in unwanted deletion in updates.
     *
     * Consumers may use the ContactPhoto and RawContactPhoto extension functions to get/set/remove
     * photos.
     */
    internal val photo: Photo?,

    /**
     * An immutable list of relations.
     *
     * If this raw contact is not associated with an Account, then this will be ignored during
     * inserts and updates.
     */
    val relations: List<Relation>,

    val sipAddress: SipAddress?,

    /**
     * An immutable list of websites.
     */
    val websites: List<Website>,

    /**
     * Map of custom mime type value to a [CustomDataEntityHolder].
     *
     * ## Developer notes
     *
     * Only mutable custom data entities are kept/handled internally to avoid having to define a
     * toMutable() and toImmutable() functions in the custom entity interface. This gives more
     * flexibility to consumers and keeps internal code lean and clean. Consumers may expose an
     * immutable version if they choose to do so.
     */
    internal val customDataEntities: Map<String, CustomDataEntityHolder>

) : RawContactEntity {

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(
            name, nickname, note, organization, sipAddress
        ) && entitiesAreAllBlank(
            addresses, emails, events, groupMemberships, ims, phones, relations, websites,
            customDataEntities.values.flatMap { it.entities }
        )

    fun toMutableRawContact() = MutableRawContact(
        id = id,
        contactId = contactId,

        addresses = addresses.asSequence().map { it.toMutableAddress() }.toMutableList(),

        emails = emails.asSequence().map { it.toMutableEmail() }.toMutableList(),

        events = events.asSequence().map { it.toMutableEvent() }.toMutableList(),

        groupMemberships = groupMemberships.toMutableList(),

        ims = ims.asSequence().map { it.toMutableIm() }.toMutableList(),

        name = name?.toMutableName(),

        nickname = nickname?.toMutableNickname(),

        note = note?.toMutableNote(),

        organization = organization?.toMutableOrganization(),

        phones = phones.asSequence().map { it.toMutablePhone() }.toMutableList(),

        photo = photo,

        relations = relations.asSequence().map { it.toMutableRelation() }.toMutableList(),

        sipAddress = sipAddress?.toMutableSipAddress(),

        websites = websites.asSequence().map { it.toMutableWebsite() }.toMutableList(),

        customDataEntities = customDataEntities.toMutableMap() // send a shallow copy
    )
}

/**
 * A mutable [RawContact].
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
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

    // The Data table contains the display name for Contacts, not for RawContacts.

    /**
     * Mutable version of [RawContact.addresses].
     */
    var addresses: MutableList<MutableAddress>,

    /**
     * Mutable version of [RawContact.emails].
     */
    var emails: MutableList<MutableEmail>,

    /**
     * Mutable version of [RawContact.events].
     *
     * If this raw contact is not associated with an Account, then this will be ignored during
     * inserts and updates.
     */
    var events: MutableList<MutableEvent>,

    /**
     * Mutable version of [RawContact.groupMemberships].
     *
     * ## Note
     *
     * If this raw contact is not associated with an Account, then this will be ignored during
     * inserts and updates. Only group memberships to groups that belong to the same account as the
     * raw contact will be inserted.
     *
     * Group membership to the account's default group will not be deleted (in the database) even
     * if it is removed from this list!
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

    // Use the RawContactOptions extension functions to get/set options.
    // The Data table contains the options columns for Contacts, not for RawContacts.

    /**
     * Mutable version of [RawContact.organization].
     */
    var organization: MutableOrganization?,

    /**
     * Mutable version of [RawContact.phones].
     */
    var phones: MutableList<MutablePhone>,

    /**
     * The [Photo] class does not have any real functional value. This exist only to prevent
     * RawContacts from being considered blanks, which may result in unwanted deletion in updates.
     *
     * Consumers may use the ContactPhoto and RawContactPhoto extension functions to get/set/remove
     * photos.
     */
    internal var photo: Photo?,

    /**
     * Mutable version of [RawContact.relations].
     *
     * If this raw contact is not associated with an Account, then this will be ignored during
     * inserts and updates.
     */
    var relations: MutableList<MutableRelation>,

    /**
     * Mutable version of [RawContact.sipAddress].
     */
    var sipAddress: MutableSipAddress?,

    /**
     * Mutable version of [RawContact.websites].
     */
    var websites: MutableList<MutableWebsite>,

    /**
     * See [RawContact.customDataEntities].
     */
    internal val customDataEntities: MutableMap<String, CustomDataEntityHolder>

) : RawContactEntity {

    constructor() : this(
        null, null, mutableListOf(), mutableListOf(), mutableListOf(),
        mutableListOf(), mutableListOf(), null, null, null, null,
        mutableListOf(), null, mutableListOf(), null, mutableListOf(), mutableMapOf()
    )

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(
            name, nickname, note, organization, photo, sipAddress
        ) && entitiesAreAllBlank(
            addresses, emails, events, groupMemberships, ims, phones, relations, websites,
            customDataEntities.values.flatMap { it.entities }
        )
}

/**
 * A blank [RawContactEntity] that contains no data (e.g. email, phone), although display names are
 * available. This only contains critical information for performing RawContact operations.
 */
@Parcelize
data class BlankRawContact internal constructor(
    override val id: Long?,
    override val contactId: Long?,

    /**
     * The RawContact's display name (given name first), which may be different from the parent
     * Contact's display name if it is made up of more than one RawContact.
     */
    // This can only be retrieved from RawContacts table queries. The Data table contains the
    // display name for Contacts, not for RawContacts.
    val displayNamePrimary: String?,

    /**
     * The RawContact's display name (family name first), which may be different from the parent
     * Contact's display name if it is made up of more than one RawContact.
     */
    // This can only be retrieved from RawContacts table queries. The Data table contains the
    // display name for Contacts, not for RawContacts.
    val displayNameAlt: String?

) : RawContactEntity {

    @IgnoredOnParcel
    override val isBlank: Boolean = true
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

    var addresses: MutableList<Address>,
    var emails: MutableList<Email>,
    var events: MutableList<Event>,
    var groupMemberships: MutableList<GroupMembership>,
    var ims: MutableList<Im>,
    var name: Name?,
    var nickname: Nickname?,
    var note: Note?,
    var organization: Organization?,
    var phones: MutableList<Phone>,
    var photo: Photo?,
    var relations: MutableList<Relation>,
    var sipAddress: SipAddress?,
    var websites: MutableList<Website>,
    internal val customDataEntities: MutableMap<String, CustomDataEntityHolder>

) : RawContactEntity {

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(
            name, nickname, note, organization, photo, sipAddress
        ) && entitiesAreAllBlank(
            addresses, emails, events, groupMemberships, ims, phones, relations, websites,
            customDataEntities.values.flatMap { it.entities }
        )

    fun toRawContact() = RawContact(
        id = id,
        contactId = contactId,

        addresses = addresses.toList(),

        emails = emails.toList(),

        events = events.toList(),

        groupMemberships = groupMemberships.toList(),

        ims = ims.toList(),

        name = name,

        nickname = nickname,

        note = note,

        organization = organization,

        phones = phones.toList(),

        photo = photo,

        relations = relations.toList(),

        sipAddress = sipAddress,

        websites = websites.toList(),

        customDataEntities = customDataEntities.toMap() // send a shallow copy
    )
}