package contacts.core.entities

import contacts.core.entities.custom.AbstractCustomDataEntityHolder
import contacts.core.entities.custom.CustomDataEntityHolder
import contacts.core.entities.custom.ImmutableCustomDataEntityHolder
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

    /**
     * A list of [EmailEntity].
     */
    val emails: List<EmailEntity>

    /**
     * A list of [EventEntity].
     *
     * If this raw contact is not associated with an Account, then this will be ignored during
     * inserts and updates.
     */
    val events: List<EventEntity>

    /**
     * A list of [GroupMembershipEntity].
     *
     * If this raw contact is not associated with an Account, then this will be ignored during
     * inserts and updates.
     */
    val groupMemberships: List<GroupMembershipEntity>

    /**
     * A list of [ImEntity].
     */
    val ims: List<ImEntity>

    /**
     * The [NameEntity].
     */
    val name: NameEntity?

    /**
     * The [NicknameEntity].
     */
    val nickname: NicknameEntity?

    /**
     * The [NoteEntity].
     */
    val note: NoteEntity?

    /**
     * The [OrganizationEntity].
     */
    val organization: OrganizationEntity?

    /**
     * A list of [PhoneEntity].
     */
    val phones: List<PhoneEntity>

    /**
     * The [PhotoEntity] does not have any real functional value. This exist only to prevent
     * RawContacts from being considered blanks, which may result in unwanted deletion in updates.
     *
     * Consumers may use the ContactPhoto and RawContactPhoto extension functions to get/set/remove
     * photos.
     */
    // This should actually be internal... if interfaces allowed for internal property declarations.
    /* internal */ val photo: PhotoEntity?

    /**
     * A list [RelationEntity].
     */
    val relations: List<RelationEntity>

    /**
     * The [SipAddressEntity].
     */
    val sipAddress: SipAddressEntity?

    /**
     * A list [WebsiteEntity].
     */
    val websites: List<WebsiteEntity>

    /**
     * Map of custom mime type value to a [ImmutableCustomDataEntityHolder].
     */
    // This should actually be internal... if interfaces allowed for internal property declarations.
    // We can put this map as an internal property of a public class... but nah. We'll see =)
    /* internal */ val customDataEntities: Map<String, AbstractCustomDataEntityHolder>

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
    override val emails: List<Email>,
    override val events: List<Event>,
    override val groupMemberships: List<GroupMembership>,
    override val ims: List<Im>,
    override val name: Name?,
    override val nickname: Nickname?,
    override val note: Note?,
    override val organization: Organization?,
    override val phones: List<Phone>,
    override val photo: Photo?,
    override val relations: List<Relation>,
    override val sipAddress: SipAddress?,
    override val websites: List<Website>,
    override val customDataEntities: Map<String, ImmutableCustomDataEntityHolder>

) : RawContactEntity, ImmutableEntityWithMutableType<MutableRawContact> {

    override fun mutableCopy() = MutableRawContact(
        id = id,
        contactId = contactId,

        addresses = addresses.asSequence().mutableCopies().toMutableList(),
        emails = emails.asSequence().mutableCopies().toMutableList(),
        events = events.asSequence().mutableCopies().toMutableList(),
        groupMemberships = groupMemberships.toMutableList(),
        ims = ims.asSequence().mutableCopies().toMutableList(),
        name = name?.mutableCopy(),
        nickname = nickname?.mutableCopy(),
        note = note?.mutableCopy(),
        organization = organization?.mutableCopy(),
        phones = phones.asSequence().mutableCopies().toMutableList(),
        photo = photo,
        relations = relations.asSequence().mutableCopies().toMutableList(),
        sipAddress = sipAddress?.mutableCopy(),
        websites = websites.asSequence().mutableCopies().toMutableList(),

        customDataEntities = customDataEntities
            .mapValues { it.value.toCustomDataEntityHolder() }
            .toMutableMap()
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
    override var emails: MutableList<MutableEmail>,
    override var events: MutableList<MutableEvent>,
    override var groupMemberships: MutableList<GroupMembership>,
    override var ims: MutableList<MutableIm>,
    override var name: MutableName?,
    override var nickname: MutableNickname?,
    override var note: MutableNote?,
    override var organization: MutableOrganization?,
    override var phones: MutableList<MutablePhone>,
    override var photo: Photo?,
    override var relations: MutableList<MutableRelation>,
    override var sipAddress: MutableSipAddress?,
    override var websites: MutableList<MutableWebsite>,

    override val customDataEntities: MutableMap<String, CustomDataEntityHolder>

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

    override val isBlank: Boolean
        get() = true

    override val addresses: List<AddressEntity>
        get() = emptyList()

    override val emails: List<EmailEntity>
        get() = emptyList()

    override val events: List<EventEntity>
        get() = emptyList()

    override val groupMemberships: List<GroupMembershipEntity>
        get() = emptyList()

    override val ims: List<ImEntity>
        get() = emptyList()

    override val name: NameEntity?
        get() = null

    override val nickname: NicknameEntity?
        get() = null

    override val note: NoteEntity?
        get() = null

    override val organization: OrganizationEntity?
        get() = null

    override val phones: List<PhoneEntity>
        get() = emptyList()

    override val photo: PhotoEntity?
        get() = null

    override val relations: List<RelationEntity>
        get() = emptyList()

    override val sipAddress: SipAddressEntity?
        get() = null

    override val websites: List<WebsiteEntity>
        get() = emptyList()

    override val customDataEntities: Map<String, AbstractCustomDataEntityHolder>
        get() = emptyMap()
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
    override var emails: MutableList<Email>,
    override var events: MutableList<Event>,
    override var groupMemberships: MutableList<GroupMembership>,
    override var ims: MutableList<Im>,
    override var name: Name?,
    override var nickname: Nickname?,
    override var note: Note?,
    override var organization: Organization?,
    override var phones: MutableList<Phone>,
    override var photo: Photo?,
    override var relations: MutableList<Relation>,
    override var sipAddress: SipAddress?,
    override var websites: MutableList<Website>,
    override val customDataEntities: MutableMap<String, ImmutableCustomDataEntityHolder>

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