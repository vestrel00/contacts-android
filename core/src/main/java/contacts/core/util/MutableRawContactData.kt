package contacts.core.util

import contacts.core.entities.*

/**
 * Adds the given [address] to [MutableRawContact.addresses].
 */
fun MutableRawContact.addAddress(address: MutableAddress) {
    addresses.add(address)
}

/**
 * Adds a new address (configured by [configureAddress]) to [MutableRawContact.addresses].
 */
fun MutableRawContact.addAddress(configureAddress: MutableAddress.() -> Unit) {
    addresses.add(MutableAddress().apply(configureAddress))
}

/**
 * Removes all instances of the given [address] from [MutableRawContact.addresses].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeAddress(address: MutableAddress, byReference: Boolean = false) {
    addresses.removeAll(address, byReference)
}

/**
 * Clears [MutableRawContact.addresses].
 */
fun MutableRawContact.removeAllAddresses() {
    addresses.clear()
}

/**
 * Adds the given [email] to [MutableRawContact.emails].
 */
fun MutableRawContact.addEmail(email: MutableEmail) {
    emails.add(email)
}

/**
 * Adds a new email (configured by [configureEmail]) to [MutableRawContact.emails].
 */
fun MutableRawContact.addEmail(configureEmail: MutableEmail.() -> Unit) {
    emails.add(MutableEmail().apply(configureEmail))
}

/**
 * Removes all instances of the given [email] from [MutableRawContact.emails].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeEmail(email: MutableEmail, byReference: Boolean = false) {
    emails.removeAll(email, byReference)
}

/**
 * Clears [MutableRawContact.emails].
 */
fun MutableRawContact.removeAllEmails() {
    emails.clear()
}

/**
 * Adds the given [event] to [MutableRawContact.events].
 */
fun MutableRawContact.addEvent(event: MutableEvent) {
    events.add(event)
}

/**
 * Adds a new event (configured by [configureEvent]) to [MutableRawContact.events].
 */
fun MutableRawContact.addEvent(configureEvent: MutableEvent.() -> Unit) {
    events.add(MutableEvent().apply(configureEvent))
}

/**
 * Removes all instances of the given [event] from [MutableRawContact.events]
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeEvent(event: MutableEvent, byReference: Boolean = false) {
    events.removeAll(event, byReference)
}

/**
 * Clears [MutableRawContact.events].
 */
fun MutableRawContact.removeAllEvents() {
    events.clear()
}

/**
 * Adds the given [groupMembership] to [MutableRawContact.groupMemberships].
 *
 * ## Note
 *
 * If this raw contact is not associated with an Account, then this will be ignored during inserts
 * and updates. Only group memberships to groups that belong to the same account as the raw contact
 * will be inserted.
 */
fun MutableRawContact.addGroupMembership(groupMembership: GroupMembership) {
    groupMemberships.add(groupMembership)
}

/**
 * Removes all instances of the given [groupMembership] from [MutableRawContact.groupMemberships].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 *
 * ## Note
 *
 * Group membership to the account's default group will not be deleted (in the database) even if it
 * is removed from this list!
 */
@JvmOverloads
fun MutableRawContact.removeGroupMembership(
    groupMembership: GroupMembership, byReference: Boolean = false
) {
    groupMemberships.removeAll(groupMembership, byReference)
}

/**
 * Clears [MutableRawContact.groupMemberships].
 */
fun MutableRawContact.removeAllGroupMemberships() {
    groupMemberships.clear()
}

/**
 * Adds the given [im] to [MutableRawContact.ims].
 */
fun MutableRawContact.addIm(im: MutableIm) {
    ims.add(im)
}

/**
 * Adds a new IM (configured by [configureIm]) to [MutableRawContact.ims].
 */
fun MutableRawContact.addIm(configureIm: MutableIm.() -> Unit) {
    ims.add(MutableIm().apply(configureIm))
}

/**
 * Removes all instances of the given [im] from [MutableRawContact.ims].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeIm(im: MutableIm, byReference: Boolean = false) {
    ims.removeAll(im, byReference)
}

/**
 * Clears [MutableRawContact.ims].
 */
fun MutableRawContact.removeAllIms() {
    ims.clear()
}

/**
 * Sets the [MutableRawContact.name].
 */
fun MutableRawContact.setName(name: MutableName?) {
    this.name = name
}

/**
 * Sets the [MutableRawContact.name] (configured by [configureName]).
 */
fun MutableRawContact.setName(configureName: MutableName.() -> Unit) {
    this.name = MutableName().apply(configureName)
}

/**
 * Sets the [MutableRawContact.nickname].
 */
fun MutableRawContact.setNickname(nickname: MutableNickname?) {
    this.nickname = nickname
}

/**
 * Sets the [MutableRawContact.nickname] (configured by [configureNickname]).
 */
fun MutableRawContact.setNickname(configureNickname: MutableNickname.() -> Unit) {
    this.nickname = MutableNickname().apply(configureNickname)
}

/**
 * Sets the [MutableRawContact.note].
 */
fun MutableRawContact.setNote(note: MutableNote?) {
    this.note = note
}

/**
 * Sets the [MutableRawContact.note] (configured by [configureNote]).
 */
fun MutableRawContact.setNote(configureNote: MutableNote.() -> Unit) {
    this.note = MutableNote().apply(configureNote)
}

// Options intentionally left out because a Contact and associated RawContacts have independent
// Options.

/**
 * Sets the [MutableRawContact.organization].
 */
fun MutableRawContact.setOrganization(organization: MutableOrganization?) {
    this.organization = organization
}

/**
 * Sets the [MutableRawContact.organization] (configured by [configureOrganization]).
 */
fun MutableRawContact.setOrganization(configureOrganization: MutableOrganization.() -> Unit) {
    this.organization = MutableOrganization().apply(configureOrganization)
}

/**
 * Adds the given [phone] to [MutableRawContact.phones].
 */
fun MutableRawContact.addPhone(phone: MutablePhone) {
    phones.add(phone)
}

/**
 * Adds a new phone (configured by [configurePhone]) to [MutableRawContact.phones].
 */
fun MutableRawContact.addPhone(configurePhone: MutablePhone.() -> Unit) {
    phones.add(MutablePhone().apply(configurePhone))
}

/**
 * Removes all instances of the given [phone] from [MutableRawContact.phones].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removePhone(phone: MutablePhone, byReference: Boolean = false) {
    phones.removeAll(phone, byReference)
}

/**
 * Clears [MutableRawContact.phones].
 */
fun MutableRawContact.removeAllPhones() {
    phones.clear()
}

// Photo intentionally left out because a Contact and associated RawContacts have independent
// Photos.

/**
 * Adds the given [relation] to [MutableRawContact.relations].
 */
fun MutableRawContact.addRelation(relation: MutableRelation) {
    relations.add(relation)
}

/**
 * Adds a new relation (configured by [configureRelation]) to [MutableRawContact.relations].
 */
fun MutableRawContact.addRelation(configureRelation: MutableRelation.() -> Unit) {
    relations.add(MutableRelation().apply(configureRelation))
}

/**
 * Removes all instances of the given [relation] from [MutableRawContact.relations].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeRelation(relation: MutableRelation, byReference: Boolean = false) {
    relations.removeAll(relation, byReference)
}

/**
 * Clears [MutableRawContact.relations].
 */
fun MutableRawContact.removeAllRelations() {
    relations.clear()
}

/**
 * Sets the [MutableRawContact.sipAddress].
 */
fun MutableRawContact.setSipAddress(sipAddress: MutableSipAddress?) {
    this.sipAddress = sipAddress
}

/**
 * Sets the [MutableRawContact.sipAddress] (configured by [configureSipAddress]).
 */
fun MutableRawContact.setSipAddress(configureSipAddress: MutableSipAddress.() -> Unit) {
    this.sipAddress = MutableSipAddress().apply(configureSipAddress)
}

/**
 * Adds the given [website] to [MutableRawContact.websites].
 */
fun MutableRawContact.addWebsite(website: MutableWebsite) {
    websites.add(website)
}

/**
 * Adds a new website (configured by [configureWebsite]) to [MutableRawContact.websites].
 */
fun MutableRawContact.addWebsite(configureWebsite: MutableWebsite.() -> Unit) {
    websites.add(MutableWebsite().apply(configureWebsite))
}

/**
 * Removes all instances of the given [website] from [MutableRawContact.websites].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeWebsite(website: MutableWebsite, byReference: Boolean = false) {
    websites.removeAll(website, byReference)
}

/**
 * Clears [MutableRawContact.websites].
 */
fun MutableRawContact.removeAllWebsites() {
    websites.clear()
}