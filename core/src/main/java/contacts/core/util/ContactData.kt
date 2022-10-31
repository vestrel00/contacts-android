package contacts.core.util

import contacts.core.entities.*

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// region Contact

/**
 * Sequence of addresses from all [Contact.rawContacts] ordered by the [Address.id].
 */
fun Contact.addresses(): Sequence<Address> = rawContacts
    .asSequence()
    .flatMap { it.addresses.asSequence() }
    .sortedBy { it.id }

/**
 * List of addresses from all [Contact.rawContacts] ordered by the [Address.id].
 */
fun Contact.addressList(): List<Address> = addresses().toList()

/**
 * Sequence of emails from all [Contact.rawContacts] ordered by the [Email.id].
 */
fun Contact.emails(): Sequence<Email> = rawContacts
    .asSequence()
    .flatMap { it.emails.asSequence() }
    .sortedBy { it.id }

/**
 * List of emails from all [Contact.rawContacts] ordered by the [Email.id].
 */
fun Contact.emailList(): List<Email> = emails().toList()

/**
 * Sequence of events from all [Contact.rawContacts] ordered by the [Event.id].
 */
fun Contact.events(): Sequence<Event> = rawContacts
    .asSequence()
    .flatMap { it.events.asSequence() }
    .sortedBy { it.id }

/**
 * List of events from all [Contact.rawContacts] ordered by the [Event.id].
 */
fun Contact.eventList(): List<Event> = events().toList()

/**
 * Sequence of group memberships from all [Contact.rawContacts] ordered by the [GroupMembership.id].
 *
 * ## Adding or removing group memberships
 *
 * Adding or removing group memberships must be done at the RawContact level, not via Contact.
 * Keep reading for why.
 *
 * ## Groups (and memberships to those groups) are tied to an Account via RawContacts
 *
 * Groups are tied to an [android.accounts.Account]. There can be no group that exist without an
 * associated Account. Therefore, memberships to groups ([GroupMembership]s) are also tied to an
 * Account. This means that Contacts do NOT have group memberships. Rather, only RawContacts can
 * have group memberships.
 *
 * For example, if there are two Accounts in the device,
 *
 * 1. "john@gmail.com"
 * 2. "doe@gmail.com"
 *
 * Then there are two sets of the same system groups plus any other groups that come with the
 * account via sync. While this set of groups may differ slightly from one OEM to another, they are
 * generally the same. The above two accounts could have the following groups.
 *
 * Groups from "john@gmail.com" account;
 *
 * - Group id: 1, systemId: Contacts, title: My Contacts, accountName: john@gmail.com, accountType: com.google
 * - Group id: 2, systemId: null, title: Starred in Android, accountName: john@gmail.com, accountType: com.google
 * - Group id: 3, systemId: Friends,title: Friends, accountName: john@gmail.com, accountType: com.google
 * - Group id: 4, systemId: Family, title: Family, accountName: john@gmail.com, accountType: com.google
 * - Group id: 5, systemId: Coworkers, title: Coworkers, accountName: john@gmail.com, accountType: com.google
 *
 * Groups from "doe@gmail.com" account;
 *
 * - Group id: 6, systemId: Contacts, title: My Contacts, accountName: doe@gmail.com, accountType: com.google
 * - Group id: 7, systemId: null, title: Starred in Android, accountName: doe@gmail.com, accountType: com.google
 * - Group id: 8, systemId: Friends,title: Friends, accountName: doe@gmail.com, accountType: com.google
 * - Group id: 9, systemId: Family, title: Family, accountName: doe@gmail.com, accountType: com.google
 * - Group id: 10, systemId: Coworkers, title: Coworkers, accountName: doe@gmail.com, accountType: com.google
 *
 * Notice that there are two sets of the same systemId and title but they come from different accounts
 * and have their own unique id in the Groups table.
 *
 * ## What is the whole point of the above lecture?
 *
 * The point is that if this [Contact] is made up of two or more [RawContact]s that belong to
 * different accounts, then this set of group memberships may contain two memberships to what
 * may seem like the same group. For example, a membership to;
 *
 * - Group id: 1, systemId: Contacts, title: My Contacts, accountName: john@gmail.com, accountType: com.google
 * - Group id: 6, systemId: Contacts, title: My Contacts, accountName: doe@gmail.com, accountType: com.google
 *
 * In the above examples, groups 1 and 6 are the default system group, which all RawContacts for the
 * respective accounts get automatically added to. Consumers may think this is a bug BUT it should
 * be clear that the two memberships are different given that they have different
 * [GroupMembership.groupId]s!
 *
 * **Furthermore**, this all means that adding or removing group memberships must be done at the
 * RawContact level! Remember, group memberships are RawContact-specific. This convenience function
 * just provides a list of all group memberships of all RawContacts associated with this Contact.
 */
fun Contact.groupMemberships(): Sequence<GroupMembership> = rawContacts
    .asSequence()
    .flatMap { it.groupMemberships.asSequence() }
    .sortedBy { it.id }

/**
 * The [groupMemberships] as a list.
 */
fun Contact.groupMembershipList(): List<GroupMembership> = groupMemberships().toList()

/**
 * Sequence of Ims from all [Contact.rawContacts] ordered by the [Im.id].
 */
fun Contact.ims(): Sequence<Im> = rawContacts
    .asSequence()
    .flatMap { it.ims.asSequence() }
    .sortedBy { it.id }

/**
 * List of Ims from all [Contact.rawContacts] ordered by the [Im.id].
 */
fun Contact.imList(): List<Im> = ims().toList()

/**
 * Sequence of names from all [Contact.rawContacts] ordered by the [Name.id].
 */
fun Contact.names(): Sequence<Name> = rawContacts
    .asSequence()
    .mapNotNull { it.name }
    .sortedBy { it.id }

/**
 * List of names from all [Contact.rawContacts] ordered by the [Name.id].
 */
fun Contact.nameList(): List<Name> = names().toList()

/**
 * Sequence of nicknames from all [Contact.rawContacts] ordered by the [Nickname.id].
 */
fun Contact.nicknames(): Sequence<Nickname> = rawContacts
    .asSequence()
    .mapNotNull { it.nickname }
    .sortedBy { it.id }

/**
 * List of nicknames from all [Contact.rawContacts] ordered by the [Nickname.id].
 */
fun Contact.nicknameList(): List<Nickname> = nicknames().toList()

/**
 * Sequence of notes from all [Contact.rawContacts] ordered by the [Note.id].
 */
fun Contact.notes(): Sequence<Note> = rawContacts
    .asSequence()
    .mapNotNull { it.note }
    .sortedBy { it.id }

/**
 * List of notes from all [Contact.rawContacts] ordered by the [Note.id].
 */
fun Contact.noteList(): List<Note> = notes().toList()

/**
 * This contact's [Options].
 */
fun Contact.options(): Options? = options

/**
 * Sequence of organizations from all [Contact.rawContacts] ordered by the [Organization.id].
 */
fun Contact.organizations(): Sequence<Organization> = rawContacts
    .asSequence()
    .mapNotNull { it.organization }
    .sortedBy { it.id }

/**
 * List of organizations from all [Contact.rawContacts] ordered by the [Organization.id].
 */
fun Contact.organizationList(): List<Organization> = organizations().toList()

/**
 * Sequence of phones from all [Contact.rawContacts] ordered by the [Phone.id].
 */
fun Contact.phones(): Sequence<Phone> = rawContacts
    .asSequence()
    .flatMap { it.phones.asSequence() }
    .sortedBy { it.id }

/**
 * List of phones from all [Contact.rawContacts] ordered by the [Phone.id].
 */
fun Contact.phoneList(): List<Phone> = phones().toList()

/**
 * Sequence of relations from all [Contact.rawContacts] ordered by the [Relation.id].
 */
fun Contact.relations(): Sequence<Relation> = rawContacts
    .asSequence()
    .flatMap { it.relations.asSequence() }
    .sortedBy { it.id }

/**
 * List of relations from all [Contact.rawContacts] ordered by the [Relation.id].
 */
fun Contact.relationList(): List<Relation> = relations().toList()

/**
 * Sequence of SIP addresses from all [Contact.rawContacts] ordered by the [SipAddress.id].
 */
fun Contact.sipAddresses(): Sequence<SipAddress> = rawContacts
    .asSequence()
    .mapNotNull { it.sipAddress }
    .sortedBy { it.id }

/**
 * List of SIP addresses from all [Contact.rawContacts] ordered by the [SipAddress.id].
 */
fun Contact.sipAddressList(): List<SipAddress> = sipAddresses().toList()

/**
 * Sequence of websites from all [Contact.rawContacts] ordered by the [Website.id].
 */
fun Contact.websites(): Sequence<Website> = rawContacts
    .asSequence()
    .flatMap { it.websites.asSequence() }
    .sortedBy { it.id }

/**
 * List of websites from all [Contact.rawContacts] ordered by the [Website.id].
 */
fun Contact.websiteList(): List<Website> = websites().toList()

/**
 * Sequence of custom data entities from all [Contact.rawContacts] matching the given [mimeType].
 */
internal fun Contact.customDataSequenceOf(
    mimeType: MimeType.Custom
): Sequence<ImmutableCustomDataEntity> = rawContacts
    .asSequence()
    .mapNotNull { it.customDataEntities[mimeType.value] }
    .flatMap {
        it.entities.asSequence()
    }

// endregion

// region MutableContact

/**
 * Sequence of addresses from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.addresses(): Sequence<MutableAddressEntity> = rawContacts
    .asSequence()
    .flatMap { it.addresses.asSequence() }
    .sortedById()

/**
 * List of addresses from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.addressList(): List<MutableAddressEntity> = addresses().toList()

/**
 * Adds the given [address] to the list of [MutableRawContact.addresses] of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addAddress(address: MutableAddressEntity) {
    rawContacts.firstOrNull()?.addAddress(address)
}

/**
 * Adds a new address (configured by [configureAddress]) to the list of
 * [MutableRawContact.addresses] of the first [MutableRawContact] in [MutableContact.rawContacts]
 * sorted by the [MutableRawContact.id].
 */
fun MutableContact.addAddress(configureAddress: NewAddress.() -> Unit) {
    rawContacts.firstOrNull()?.addAddress(configureAddress)
}

/**
 * Removes all instances of the given [address] from all of the [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableContact.removeAddress(address: MutableAddressEntity, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.removeAddress(address, byReference)
    }
}

/**
 * Removes all [addresses] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllAddresses() {
    for (rawContact in rawContacts) {
        rawContact.removeAllAddresses()
    }
}

/**
 * Sequence of emails from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.emails(): Sequence<MutableEmailEntity> = rawContacts
    .asSequence()
    .flatMap { it.emails.asSequence() }
    .sortedById()

/**
 * List of emails from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.emailList(): List<MutableEmailEntity> = emails().toList()

/**
 * Adds the given [email] to the list of [MutableRawContact.emails] of the first [MutableRawContact]
 * in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addEmail(email: MutableEmailEntity) {
    rawContacts.firstOrNull()?.addEmail(email)
}

/**
 * Adds a new email (configured by [configureEmail]) to the list of [MutableRawContact.emails] of
 * the first [MutableRawContact] in [MutableContact.rawContacts] sorted by the
 * [MutableRawContact.id].
 */
fun MutableContact.addEmail(configureEmail: NewEmail.() -> Unit) {
    rawContacts.firstOrNull()?.addEmail(configureEmail)
}

/**
 * Removes all instances of the given [email] from all of the [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableContact.removeEmail(email: MutableEmailEntity, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.removeEmail(email, byReference)
    }
}

/**
 * Removes all [emails] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllEmails() {
    for (rawContact in rawContacts) {
        rawContact.removeAllEmails()
    }
}

/**
 * Sequence of events from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.events(): Sequence<MutableEventEntity> = rawContacts
    .asSequence()
    .flatMap { it.events.asSequence() }
    .sortedById()

/**
 * List of events from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.eventList(): List<MutableEventEntity> = events().toList()

/**
 * Adds the given [event] to the list of [MutableRawContact.events] of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addEvent(event: MutableEventEntity) {
    rawContacts.firstOrNull()?.addEvent(event)
}

/**
 * Adds a new event (configured by [configureEvent]) to the list of [MutableRawContact.events] of
 * the first [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addEvent(configureEvent: NewEvent.() -> Unit) {
    rawContacts.firstOrNull()?.addEvent(configureEvent)
}

/**
 * Removes all instances of the given [event] from all of the [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableContact.removeEvent(event: MutableEventEntity, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.removeEvent(event, byReference)
    }
}

/**
 * Removes all [events] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllEvents() {
    for (rawContact in rawContacts) {
        rawContact.removeAllEvents()
    }
}

/**
 * The [groupMemberships] as a sequence.
 */
fun MutableContact.groupMemberships(): Sequence<GroupMembershipEntity> = rawContacts
    .asSequence()
    .flatMap { it.groupMemberships.asSequence() }
    .sortedById()

/**
 * The [groupMemberships] as a list.
 */
fun MutableContact.groupMembershipList(): List<GroupMembershipEntity> = groupMemberships().toList()

// GroupMemberships add and remove functions are intentionally left out because they should never
// be combined. The native Contacts app hides the group membership UI field when viewing/editing a
// contact with more than one RawContact.

/**
 * Sequence of Ims from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.ims(): Sequence<MutableImEntity> = rawContacts
    .asSequence()
    .flatMap { it.ims.asSequence() }
    .sortedById()

/**
 * List of IMs from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.imList(): List<MutableImEntity> = ims().toList()

/**
 * Adds the given [im] to the list of [MutableRawContact.ims] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addIm(im: MutableImEntity) {
    rawContacts.firstOrNull()?.addIm(im)
}

/**
 * Adds a new IM (configured by [configureIm]) to the list of [MutableRawContact.ims] of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addIm(configureIm: NewIm.() -> Unit) {
    rawContacts.firstOrNull()?.addIm(configureIm)
}

/**
 * Removes all instances of the given [im] from all of the [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableContact.removeIm(im: MutableImEntity, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.removeIm(im, byReference)
    }
}

/**
 * Removes all [ims] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllIms() {
    for (rawContact in rawContacts) {
        rawContact.removeAllIms()
    }
}

/**
 * Sequence of names from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.names(): Sequence<MutableNameEntity> = rawContacts
    .asSequence()
    .mapNotNull { it.name }
    .sortedById()

/**
 * List of names from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.nameList(): List<MutableNameEntity> = names().toList()

/**
 * Sets the [MutableRawContact.name] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setName(name: MutableNameEntity?) {
    rawContacts.firstOrNull()?.setName(name)
}

/**
 * Sets the [MutableRawContact.name] (configured by [configureName]) of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id]
 * to a new name.
 */
fun MutableContact.setName(configureName: NewName.() -> Unit) {
    rawContacts.firstOrNull()?.setName(configureName)
}

/**
 * Sequence of nicknames from all [MutableContact.rawContacts] ordered by the id.
 */
fun MutableContact.nicknames(): Sequence<MutableNicknameEntity> = rawContacts
    .asSequence()
    .mapNotNull { it.nickname }
    .sortedById()

/**
 * List of nicknames from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.nicknameList(): List<MutableNicknameEntity> = nicknames().toList()

/**
 * Sets the [MutableRawContact.nickname] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setNickname(nickname: MutableNicknameEntity?) {
    rawContacts.firstOrNull()?.setNickname(nickname)
}

/**
 * Sets the [MutableRawContact.nickname] (configured by [configureNickname]) of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id] to a
 * new nickname.
 */
fun MutableContact.setNickname(configureNickname: NewNickname.() -> Unit) {
    rawContacts.firstOrNull()?.setNickname(configureNickname)
}

/**
 * Sequence of notes from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.notes(): Sequence<MutableNoteEntity> = rawContacts
    .asSequence()
    .mapNotNull { it.note }
    .sortedById()

/**
 * List of notes from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.noteList(): List<MutableNoteEntity> = notes().toList()

/**
 * Sets the [MutableRawContact.note] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setNote(note: MutableNoteEntity?) {
    rawContacts.firstOrNull()?.setNote(note)
}

/**
 * Sets the [MutableRawContact.note] (configured by [configureNote]) of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id] to a
 * new note.
 */
fun MutableContact.setNote(configureNote: NewNote.() -> Unit) {
    rawContacts.firstOrNull()?.setNote(configureNote)
}

/**
 * This contact's [MutableOptionsEntity].
 */
fun MutableContact.options(): MutableOptionsEntity? = options

/**
 * Sets the [MutableContact.options].
 */
fun MutableContact.setOptions(options: MutableOptionsEntity?) {
    this.options = options
}

/**
 * Sets the [MutableContact.options] (configured by [configureOptions]).
 */
fun MutableContact.setOptions(configureOptions: NewOptions.() -> Unit) {
    setOptions(NewOptions().apply(configureOptions))
}

/**
 * Sequence of organizations from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.organizations(): Sequence<MutableOrganizationEntity> = rawContacts
    .asSequence()
    .mapNotNull { it.organization }
    .sortedById()

/**
 * List of organizations from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.organizationList(): List<MutableOrganizationEntity> = organizations().toList()

/**
 * Sets the [MutableRawContact.organization] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setOrganization(organization: MutableOrganizationEntity?) {
    rawContacts.firstOrNull()?.setOrganization(organization)
}

/**
 * Sets the [MutableRawContact.organization] (configured by [configureOrganization]) of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id] to a new
 * organization.
 */
fun MutableContact.setOrganization(configureOrganization: NewOrganization.() -> Unit) {
    rawContacts.firstOrNull()?.setOrganization(configureOrganization)
}

/**
 * Sequence of phones from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.phones(): Sequence<MutablePhoneEntity> = rawContacts
    .asSequence()
    .flatMap { it.phones.asSequence() }
    .sortedById()

/**
 * List of phones from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.phoneList(): List<MutablePhoneEntity> = phones().toList()

/**
 * Adds the given [phone] to the list of [MutableRawContact.phones] of the first [MutableRawContact]
 * in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addPhone(phone: MutablePhoneEntity) {
    rawContacts.firstOrNull()?.addPhone(phone)
}

/**
 * Adds a new phone (configured by [configurePhone]) to the list of [MutableRawContact.phones] of
 * the first [MutableRawContact] in [MutableContact.rawContacts] sorted by the
 * [MutableRawContact.id] to a new phone.
 */
fun MutableContact.addPhone(configurePhone: NewPhone.() -> Unit) {
    rawContacts.firstOrNull()?.addPhone(configurePhone)
}

/**
 * Removes all instances of the given [phone] from all of the [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableContact.removePhone(phone: MutablePhoneEntity, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.removePhone(phone, byReference)
    }
}

/**
 * Removes all [phones] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllPhones() {
    for (rawContact in rawContacts) {
        rawContact.removeAllPhones()
    }
}

/**
 * Sequence of relations from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.relations(): Sequence<MutableRelationEntity> = rawContacts
    .asSequence()
    .flatMap { it.relations.asSequence() }
    .sortedById()

/**
 * List of relations from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.relationList(): List<MutableRelationEntity> = relations().toList()

/**
 * Adds the given [relation] to the list of [MutableRawContact.relations] of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addRelation(relation: MutableRelationEntity) {
    rawContacts.firstOrNull()?.addRelation(relation)
}

/**
 * Adds a new relation (configured by [configureRelation]) to the list of
 * [MutableRawContact.relations] of the first [MutableRawContact] in [MutableContact.rawContacts]
 * sorted by the [MutableRawContact.id] to a new relation.
 */
fun MutableContact.addRelation(configureRelation: NewRelation.() -> Unit) {
    rawContacts.firstOrNull()?.addRelation(configureRelation)
}

/**
 * Removes all instances of the given [relation] from all of the [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableContact.removeRelation(relation: MutableRelationEntity, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.removeRelation(relation, byReference)
    }
}

/**
 * Removes all [relations] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllRelations() {
    for (rawContact in rawContacts) {
        rawContact.removeAllRelations()
    }
}

/**
 * Sequence of SIP addresses from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.sipAddresses(): Sequence<MutableSipAddressEntity> = rawContacts
    .asSequence()
    .mapNotNull { it.sipAddress }
    .sortedById()

/**
 * List of SIP addresses from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.sipAddressList(): List<MutableSipAddressEntity> = sipAddresses().toList()

/**
 * Sets the [MutableRawContact.sipAddress] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setSipAddress(sipAddress: MutableSipAddressEntity?) {
    rawContacts.firstOrNull()?.setSipAddress(sipAddress)
}

/**
 * Sets the [MutableRawContact.sipAddress] (configured by [configureSipAddress]) of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id] to a
 * new SIP address.
 */
fun MutableContact.setSipAddress(configureSipAddress: NewSipAddress.() -> Unit) {
    rawContacts.firstOrNull()?.setSipAddress(configureSipAddress)
}

/**
 * Sequence of websites from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.websites(): Sequence<MutableWebsiteEntity> = rawContacts
    .asSequence()
    .flatMap { it.websites.asSequence() }
    .sortedById()

/**
 * List of websites from all [MutableContact.rawContacts] ordered by id.
 */
fun MutableContact.websiteList(): List<MutableWebsiteEntity> = websites().toList()

/**
 * Adds the given [website] to the list of [MutableRawContact.websites] of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addWebsite(website: MutableWebsiteEntity) {
    rawContacts.firstOrNull()?.addWebsite(website)
}

/**
 * Adds a new website (configured by [configureWebsite]) to the list of [MutableRawContact.websites]
 * of the first [MutableRawContact] in [MutableContact.rawContacts] sorted by the
 * [MutableRawContact.id].
 */
fun MutableContact.addWebsite(configureWebsite: NewWebsite.() -> Unit) {
    rawContacts.firstOrNull()?.addWebsite(configureWebsite)
}

/**
 * Removes all instances of the given [website] from all of the [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableContact.removeWebsite(website: MutableWebsiteEntity, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.removeWebsite(website, byReference)
    }
}

/**
 * Removes all [websites] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllWebsites() {
    for (rawContact in rawContacts) {
        rawContact.removeAllWebsites()
    }
}

// endregion