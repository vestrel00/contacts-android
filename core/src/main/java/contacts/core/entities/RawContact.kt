package contacts.core.entities

import contacts.core.entities.custom.AbstractCustomDataEntityHolder
import contacts.core.entities.custom.CustomDataEntityHolder
import contacts.core.entities.custom.ImmutableCustomDataEntityHolder
import contacts.core.util.isProfileId
import kotlinx.parcelize.Parcelize

/**
 * [Entity] that holds data modeling columns in the RawContacts table.
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

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from RawContactEntity, there is only one interface that extends it; ExistingRawContactEntity.
 * This interface is used for library functions that require a RawContactEntity with an ID, which means
 * that it exists in the database. There are two variants of this; RawContact and MutableRawContact.
 * With this, we can create functions (or extensions) that can take in (or have as the receiver)
 * either RawContact or MutableRawContact through the ExistingRawContactEntity abstraction/facade.
 *
 * This is why there are no interfaces for NewRawContactEntity, ImmutableRawContactEntity, and
 * MutableRawContactEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A [RawContactEntity] that has already been inserted into the database.
 */
sealed interface ExistingRawContactEntity : RawContactEntity, ExistingEntity {
    /**
     * The id of the RawContacts row this represents.
     *
     * The value of RawContacts._ID / Data.RAW_CONTACT_ID.
     */
    // Override for documentation purposes.
    override val id: Long

    /**
     * The ID of the [Contact] that this [RawContact] is associated with.
     *
     * The value of RawContacts.CONTACT_ID / Data.CONTACT_ID.
     */
    val contactId: Long

    // The Data table contains the display name for Contacts, not for RawContacts.

    /**
     * True if this raw contact belongs to the user's personal profile entry.
     */
    val isProfile: Boolean
        get() = id.isProfileId
}

/**
 * An immutable [ExistingRawContactEntity].
 *
 * This can hold existing immutable data entities.
 */
@Parcelize
data class RawContact internal constructor(

    override val id: Long,
    override val contactId: Long,

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

) : ExistingRawContactEntity, ImmutableEntityWithMutableType<MutableRawContact> {

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
 * A mutable [ExistingRawContactEntity].
 *
 * This can hold new and existing mutable data entities.
 */
@Parcelize
data class MutableRawContact internal constructor(

    override val id: Long,
    override val contactId: Long,

    override var addresses: MutableList<MutableAddressEntity>,
    override var emails: MutableList<MutableEmailEntity>,
    override var events: MutableList<MutableEventEntity>,
    override var groupMemberships: MutableList<GroupMembershipEntity>,
    override var ims: MutableList<MutableImEntity>,
    override var name: MutableNameEntity?,
    override var nickname: MutableNicknameEntity?,
    override var note: MutableNoteEntity?,
    override var organization: MutableOrganizationEntity?,
    override var phones: MutableList<MutablePhoneEntity>,
    override var photo: PhotoEntity?,
    override var relations: MutableList<MutableRelationEntity>,
    override var sipAddress: MutableSipAddressEntity?,
    override var websites: MutableList<MutableWebsiteEntity>,

    override val customDataEntities: MutableMap<String, CustomDataEntityHolder>

) : ExistingRawContactEntity, MutableEntity

/**
 * A new mutable [RawContactEntity].
 *
 * This can hold new mutable data entities.
 */
// Intentionally expose primary constructor to consumers. Useful for Kotlin users.
@Parcelize
data class NewRawContact(

    override var addresses: MutableList<NewAddress>,
    override var emails: MutableList<NewEmail>,
    override var events: MutableList<NewEvent>,
    override var groupMemberships: MutableList<GroupMembership>,
    override var ims: MutableList<NewIm>,
    override var name: NewName?,
    override var nickname: NewNickname?,
    override var note: NewNote?,
    override var organization: NewOrganization?,
    override var phones: MutableList<NewPhone>,
    override var photo: Photo?,
    override var relations: MutableList<NewRelation>,
    override var sipAddress: NewSipAddress?,
    override var websites: MutableList<NewWebsite>,

    override val customDataEntities: MutableMap<String, CustomDataEntityHolder>

) : RawContactEntity, NewEntity, MutableEntity {

    // An empty constructor for consumer use. Useful for both Kotlin and Java users.
    constructor() : this(
        mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(),
        null, null, null, null, mutableListOf(), null,
        mutableListOf(), null, mutableListOf(), mutableMapOf()
    )
}

/**
 * A blank [ExistingRawContactEntity] that contains no data (e.g. email, phone, etc), although
 * display names are available. This only contains critical information for performing RawContact
 * operations.
 */
@Parcelize
data class BlankRawContact internal constructor(
    override val id: Long,
    override val contactId: Long,

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

) : ExistingRawContactEntity, ImmutableEntity {

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
 * A temporary holder of existing immutable entities in mutable lists / attribute.
 *
 * Used internally to optimize cursor to contact mappings.
 */
@Parcelize
internal data class TempRawContact constructor(

    override val id: Long,
    override val contactId: Long,

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

) : ExistingRawContactEntity, MutableEntity {

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