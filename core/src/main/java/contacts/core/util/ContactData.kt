package contacts.core.util

import contacts.core.entities.*

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// Another dev note: Receiver signatures are the concrete types instead of the interface type.
// This is done so that consumers gets references to actual concrete types, which may implement
// other interfaces required by APIs in this library.

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

// Options intentionally left out because a Contact and associated RawContacts have independent
// Options.

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

// Photo intentionally left out because a Contact and associated RawContacts have independent
// Photos.

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
): Sequence<CustomDataEntity> = rawContacts
    .asSequence()
    .mapNotNull { it.customDataEntities[mimeType.value] }
    .flatMap {
        it.entities.asSequence()
    }

// endregion

// region MutableContact

/**
 * Sequence of addresses from all [MutableContact.rawContacts] ordered by the [MutableAddress.id].
 */
fun MutableContact.addresses(): Sequence<MutableAddress> = rawContacts
    .asSequence()
    .flatMap { it.addresses.asSequence() }
    .sortedBy { it.id }

/**
 * List of addresses from all [MutableContact.rawContacts] ordered by the [MutableAddress.id].
 */
fun MutableContact.addressList(): List<MutableAddress> = addresses().toList()

/**
 * Adds the given [address] to the list of [MutableRawContact.addresses] of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addAddress(address: MutableAddress) {
    rawContacts.firstOrNull()?.addAddress(address)
}

/**
 * Adds a new address (configured by [configureAddress]) to the list of
 * [MutableRawContact.addresses] of the first [MutableRawContact] in [MutableContact.rawContacts]
 * sorted by the [MutableRawContact.id].
 */
fun MutableContact.addAddress(configureAddress: MutableAddress.() -> Unit) {
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
fun MutableContact.removeAddress(address: MutableAddress, byReference: Boolean = false) {
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
 * Sequence of emails from all [MutableContact.rawContacts] ordered by the [MutableEmail.id].
 */
fun MutableContact.emails(): Sequence<MutableEmail> = rawContacts
    .asSequence()
    .flatMap { it.emails.asSequence() }
    .sortedBy { it.id }

/**
 * List of emails from all [MutableContact.rawContacts] ordered by the [MutableEmail.id].
 */
fun MutableContact.emailList(): List<MutableEmail> = emails().toList()

/**
 * Adds the given [email] to the list of [MutableRawContact.emails] of the first [MutableRawContact]
 * in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addEmail(email: MutableEmail) {
    rawContacts.firstOrNull()?.addEmail(email)
}

/**
 * Adds a new email (configured by [configureEmail]) to the list of [MutableRawContact.emails] of
 * the first [MutableRawContact] in [MutableContact.rawContacts] sorted by the
 * [MutableRawContact.id].
 */
fun MutableContact.addEmail(configureEmail: MutableEmail.() -> Unit) {
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
fun MutableContact.removeEmail(email: MutableEmail, byReference: Boolean = false) {
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
 * Sequence of events from all [MutableContact.rawContacts] ordered by the [MutableEvent.id].
 */
fun MutableContact.events(): Sequence<MutableEvent> = rawContacts
    .asSequence()
    .flatMap { it.events.asSequence() }
    .sortedBy { it.id }

/**
 * List of events from all [MutableContact.rawContacts] ordered by the [MutableEvent.id].
 */
fun MutableContact.eventList(): List<MutableEvent> = events().toList()

/**
 * Adds the given [event] to the list of [MutableRawContact.events] of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addEvent(event: MutableEvent) {
    rawContacts.firstOrNull()?.addEvent(event)
}

/**
 * Adds a new event (configured by [configureEvent]) to the list of [MutableRawContact.events] of
 * the first [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addEvent(configureEvent: MutableEvent.() -> Unit) {
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
fun MutableContact.removeEvent(event: MutableEvent, byReference: Boolean = false) {
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
fun MutableContact.groupMemberships(): Sequence<GroupMembership> = rawContacts
    .asSequence()
    .flatMap { it.groupMemberships.asSequence() }
    .sortedBy { it.id }

/**
 * The [groupMemberships] as a list.
 */
fun MutableContact.groupMembershipList(): List<GroupMembership> = groupMemberships().toList()

// GroupMemberships add and remove functions are intentionally left out because they should never
// be combined. The native Contacts app hides the group membership UI field when viewing/editing a
// contact with more than one RawContact.

/**
 * Sequence of Ims from all [MutableContact.rawContacts] ordered by the [MutableIm.id].
 */
fun MutableContact.ims(): Sequence<MutableIm> = rawContacts
    .asSequence()
    .flatMap { it.ims.asSequence() }
    .sortedBy { it.id }

/**
 * List of IMs from all [MutableContact.rawContacts] ordered by the [MutableIm.id].
 */
fun MutableContact.imList(): List<MutableIm> = ims().toList()

/**
 * Adds the given [im] to the list of [MutableRawContact.ims] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addIm(im: MutableIm) {
    rawContacts.firstOrNull()?.addIm(im)
}

/**
 * Adds a new IM (configured by [configureIm]) to the list of [MutableRawContact.ims] of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addIm(configureIm: MutableIm.() -> Unit) {
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
fun MutableContact.removeIm(im: MutableIm, byReference: Boolean = false) {
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
 * Sequence of names from all [MutableContact.rawContacts] ordered by the [MutableName.id].
 */
fun MutableContact.names(): Sequence<MutableName> = rawContacts
    .asSequence()
    .mapNotNull { it.name }
    .sortedBy { it.id }

/**
 * List of names from all [MutableContact.rawContacts] ordered by the [MutableName.id].
 */
fun MutableContact.nameList(): List<MutableName> = names().toList()

/**
 * Sets the [MutableRawContact.name] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setName(name: MutableName?) {
    rawContacts.firstOrNull()?.setName(name)
}

/**
 * Sets the [MutableRawContact.name] (configured by [configureName]) of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setName(configureName: MutableName.() -> Unit) {
    rawContacts.firstOrNull()?.setName(configureName)
}

/**
 * Sequence of nicknames from all [MutableContact.rawContacts] ordered by the [MutableNickname.id].
 */
fun MutableContact.nicknames(): Sequence<MutableNickname> = rawContacts
    .asSequence()
    .mapNotNull { it.nickname }
    .sortedBy { it.id }

/**
 * List of nicknames from all [MutableContact.rawContacts] ordered by the [MutableNickname.id].
 */
fun MutableContact.nicknameList(): List<MutableNickname> = nicknames().toList()

/**
 * Sets the [MutableRawContact.nickname] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setNickname(nickname: MutableNickname?) {
    rawContacts.firstOrNull()?.setNickname(nickname)
}

/**
 * Sets the [MutableRawContact.nickname] (configured by [configureNickname]) of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setNickname(configureNickname: MutableNickname.() -> Unit) {
    rawContacts.firstOrNull()?.setNickname(configureNickname)
}

/**
 * Sequence of notes from all [MutableContact.rawContacts] ordered by the [MutableNote.id].
 */
fun MutableContact.notes(): Sequence<MutableNote> = rawContacts
    .asSequence()
    .mapNotNull { it.note }
    .sortedBy { it.id }

/**
 * List of notes from all [MutableContact.rawContacts] ordered by the [MutableNote.id].
 */
fun MutableContact.noteList(): List<MutableNote> = notes().toList()

/**
 * Sets the [MutableRawContact.note] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setNote(note: MutableNote?) {
    rawContacts.firstOrNull()?.setNote(note)
}

/**
 * Sets the [MutableRawContact.note] (configured by [configureNote]) of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setNote(configureNote: MutableNote.() -> Unit) {
    rawContacts.firstOrNull()?.setNote(configureNote)
}

// Options intentionally left out because a Contact and associated RawContacts have independent
// Options.

/**
 * Sequence of organizations from all [MutableContact.rawContacts] ordered by the
 * [MutableOrganization.id].
 */
fun MutableContact.organizations(): Sequence<MutableOrganization> = rawContacts
    .asSequence()
    .mapNotNull { it.organization }
    .sortedBy { it.id }

/**
 * List of organizations from all [MutableContact.rawContacts] ordered by the
 * [MutableOrganization.id].
 */
fun MutableContact.organizationList(): List<MutableOrganization> = organizations().toList()

/**
 * Sets the [MutableRawContact.organization] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setOrganization(organization: MutableOrganization?) {
    rawContacts.firstOrNull()?.setOrganization(organization)
}

/**
 * Sets the [MutableRawContact.organization] (configured by [configureOrganization]) of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setOrganization(configureOrganization: MutableOrganization.() -> Unit) {
    rawContacts.firstOrNull()?.setOrganization(configureOrganization)
}

/**
 * Sequence of phones from all [MutableContact.rawContacts] ordered by the [MutablePhone.id].
 */
fun MutableContact.phones(): Sequence<MutablePhone> = rawContacts
    .asSequence()
    .flatMap { it.phones.asSequence() }
    .sortedBy { it.id }

/**
 * List of phones from all [MutableContact.rawContacts] ordered by the [MutablePhone.id].
 */
fun MutableContact.phoneList(): List<MutablePhone> = phones().toList()

/**
 * Adds the given [phone] to the list of [MutableRawContact.phones] of the first [MutableRawContact]
 * in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addPhone(phone: MutablePhone) {
    rawContacts.firstOrNull()?.addPhone(phone)
}

/**
 * Adds a new phone (configured by [configurePhone]) to the list of [MutableRawContact.phones] of
 * the first [MutableRawContact] in [MutableContact.rawContacts] sorted by the
 * [MutableRawContact.id].
 */
fun MutableContact.addPhone(configurePhone: MutablePhone.() -> Unit) {
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
fun MutableContact.removePhone(phone: MutablePhone, byReference: Boolean = false) {
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

// Photo intentionally left out because a Contact and associated RawContacts have independent
// Photos.

/**
 * Sequence of relations from all [MutableContact.rawContacts] ordered by the [MutableRelation.id].
 */
fun MutableContact.relations(): Sequence<MutableRelation> = rawContacts
    .asSequence()
    .flatMap { it.relations.asSequence() }
    .sortedBy { it.id }

/**
 * List of relations from all [MutableContact.rawContacts] ordered by the [MutableRelation.id].
 */
fun MutableContact.relationList(): List<MutableRelation> = relations().toList()

/**
 * Adds the given [relation] to the list of [MutableRawContact.relations] of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addRelation(relation: MutableRelation) {
    rawContacts.firstOrNull()?.addRelation(relation)
}

/**
 * Adds a new relation (configured by [configureRelation]) to the list of
 * [MutableRawContact.relations] of the first [MutableRawContact] in [MutableContact.rawContacts]
 * sorted by the [MutableRawContact.id].
 */
fun MutableContact.addRelation(configureRelation: MutableRelation.() -> Unit) {
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
fun MutableContact.removeRelation(relation: MutableRelation, byReference: Boolean = false) {
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
 * Sequence of SIP addresses from all [MutableContact.rawContacts] ordered by the
 * [MutableSipAddress.id].
 */
fun MutableContact.sipAddresses(): Sequence<MutableSipAddress> = rawContacts
    .asSequence()
    .mapNotNull { it.sipAddress }
    .sortedBy { it.id }

/**
 * List of SIP addresses from all [MutableContact.rawContacts] ordered by the
 * [MutableSipAddress.id].
 */
fun MutableContact.sipAddressList(): List<MutableSipAddress> = sipAddresses().toList()

/**
 * Sets the [MutableRawContact.sipAddress] of the first [MutableRawContact] in
 * [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setSipAddress(sipAddress: MutableSipAddress?) {
    rawContacts.firstOrNull()?.setSipAddress(sipAddress)
}

/**
 * Sets the [MutableRawContact.sipAddress] (configured by [configureSipAddress]) of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.setSipAddress(configureSipAddress: MutableSipAddress.() -> Unit) {
    rawContacts.firstOrNull()?.setSipAddress(configureSipAddress)
}

/**
 * Sequence of websites from all [MutableContact.rawContacts] ordered by the [MutableWebsite.id].
 */
fun MutableContact.websites(): Sequence<MutableWebsite> = rawContacts
    .asSequence()
    .flatMap { it.websites.asSequence() }
    .sortedBy { it.id }

/**
 * List of websites from all [MutableContact.rawContacts] ordered by the [MutableWebsite.id].
 */
fun MutableContact.websiteList(): List<MutableWebsite> = websites().toList()

/**
 * Adds the given [website] to the list of [MutableRawContact.websites] of the first
 * [MutableRawContact] in [MutableContact.rawContacts] sorted by the [MutableRawContact.id].
 */
fun MutableContact.addWebsite(website: MutableWebsite) {
    rawContacts.firstOrNull()?.addWebsite(website)
}

/**
 * Adds a new website (configured by [configureWebsite]) to the list of [MutableRawContact.websites]
 * of the first [MutableRawContact] in [MutableContact.rawContacts] sorted by the
 * [MutableRawContact.id].
 */
fun MutableContact.addWebsite(configureWebsite: MutableWebsite.() -> Unit) {
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
fun MutableContact.removeWebsite(website: MutableWebsite, byReference: Boolean = false) {
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