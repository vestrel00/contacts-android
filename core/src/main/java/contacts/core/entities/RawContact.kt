package contacts.core.entities

import contacts.core.entities.custom.CustomDataEntityHolder
import contacts.core.util.isProfileId
import kotlinx.parcelize.Parcelize

/**
 * [Entity] in the RawContacts table.
 *
 * ## Contact, RawContact, and Data
 *
 * A Contact may consist of one or more RawContact. A RawContact is an association between a Contact
 * and an [android.accounts.Account]. Each RawContact is associated with several pieces of Data such
 * as name, emails, phone, address, and more.
 *
 * The Contacts Provider may combine RawContacts from several different Accounts. The same effect
 * is achieved when merging / linking multiple contacts.
 *
 * It is possible for a RawContact to not be associated with an Account. Such RawContacts are local
 * to the device and are not synced.
 */
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

    // The Data table contains the display name for Contacts, not for RawContacts.

    /**
     * True if this raw contact belongs to the user's personal profile entry.
     */
    val isProfile: Boolean
        get() = id.isProfileId

    /**
     * A list of [AddressEntity].
     */
    val addresses: List<AddressEntity>

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(
            name, nickname, note, organization, sipAddress
        ) && entitiesAreAllBlank(
            addresses, emails, events, groupMemberships, ims, phones, relations, websites,
            customDataEntities.values.flatMap { it.entities }
        )

    // The Data table contains the options columns for Contacts, not for RawContacts.
    // Use the RawContactOptions extension functions to get/set options.
}

/**
 * An immutable [RawContactEntity].
 */
@Parcelize
data class RawContact internal constructor(

    override val id: Long?,
    override val contactId: Long?,

    override val addresses: List<Address>,

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

) : RawContactEntity, ImmutableEntityWithMutableType<MutableRawContact> {

    override fun mutableCopy() = MutableRawContact(
        id = id,
        contactId = contactId,

        addresses = addresses.mutableCopies().toMutableList(),

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
 * An mutable [RawContactEntity].
 */
@Parcelize
data class MutableRawContact internal constructor(

    override val id: Long?,
    override val contactId: Long?,

    override var addresses: MutableList<MutableAddress>,
    var emails: MutableList<MutableEmail>,
    var events: MutableList<MutableEvent>,
    var groupMemberships: MutableList<GroupMembership>,
    var ims: MutableList<MutableIm>,
    var name: MutableName?,
    var nickname: MutableNickname?,
    var note: MutableNote?,
    var organization: MutableOrganization?,
    var phones: MutableList<MutablePhone>,
    internal var photo: Photo?,
    var relations: MutableList<MutableRelation>,
    var sipAddress: MutableSipAddress?,
    var websites: MutableList<MutableWebsite>,

    internal val customDataEntities: MutableMap<String, CustomDataEntityHolder>

) : RawContactEntity, MutableEntity {

    constructor() : this(
        null, null, mutableListOf(), mutableListOf(), mutableListOf(),
        mutableListOf(), mutableListOf(), null, null, null, null,
        mutableListOf(), null, mutableListOf(), null, mutableListOf(), mutableMapOf()
    )
}

/**
 * A blank [RawContactEntity] that contains no data (e.g. email, phone, etc), although display names
 * are available. This only contains critical information for performing RawContact operations.
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

) : RawContactEntity, ImmutableEntity {

    override val addresses: List<AddressEntity>
        get() = emptyList()

    override val isBlank: Boolean
        get() = true
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

    override var addresses: MutableList<Address>,
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

) : RawContactEntity, MutableEntity {

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