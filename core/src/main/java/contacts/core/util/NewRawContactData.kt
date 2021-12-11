package contacts.core.util

import contacts.core.entities.*

/**
 * Adds the given [address] to [NewRawContact.addresses].
 */
fun NewRawContact.addAddress(address: NewAddress) {
    addresses.add(address)
}

/**
 * Adds a new address (configured by [configureAddress]) to [NewRawContact.addresses].
 */
fun NewRawContact.addAddress(configureAddress: NewAddress.() -> Unit) {
    addresses.add(NewAddress().apply(configureAddress))
}

/**
 * Removes all instances of the given [address] from [NewRawContact.addresses].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun NewRawContact.removeAddress(address: NewAddress, byReference: Boolean = false) {
    addresses.removeAll(address, byReference)
}

/**
 * Clears [NewRawContact.addresses].
 */
fun NewRawContact.removeAllAddresses() {
    addresses.clear()
}

/**
 * Adds the given [email] to [NewRawContact.emails].
 */
fun NewRawContact.addEmail(email: NewEmail) {
    emails.add(email)
}

/**
 * Adds a new email (configured by [configureEmail]) to [NewRawContact.emails].
 */
fun NewRawContact.addEmail(configureEmail: NewEmail.() -> Unit) {
    emails.add(NewEmail().apply(configureEmail))
}

/**
 * Removes all instances of the given [email] from [NewRawContact.emails].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun NewRawContact.removeEmail(email: NewEmail, byReference: Boolean = false) {
    emails.removeAll(email, byReference)
}

/**
 * Clears [NewRawContact.emails].
 */
fun NewRawContact.removeAllEmails() {
    emails.clear()
}

/**
 * Adds the given [event] to [NewRawContact.events].
 */
fun NewRawContact.addEvent(event: NewEvent) {
    events.add(event)
}

/**
 * Adds a new event (configured by [configureEvent]) to [NewRawContact.events].
 */
fun NewRawContact.addEvent(configureEvent: NewEvent.() -> Unit) {
    events.add(NewEvent().apply(configureEvent))
}

/**
 * Removes all instances of the given [event] from [NewRawContact.events]
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun NewRawContact.removeEvent(event: NewEvent, byReference: Boolean = false) {
    events.removeAll(event, byReference)
}

/**
 * Clears [NewRawContact.events].
 */
fun NewRawContact.removeAllEvents() {
    events.clear()
}

/**
 * Adds the given [groupMembership] to [NewRawContact.groupMemberships].
 *
 * ## Note
 *
 * If this raw contact is not associated with an Account, then this will be ignored during inserts
 * and updates. Only group memberships to groups that belong to the same account as the raw contact
 * will be inserted.
 */
fun NewRawContact.addGroupMembership(groupMembership: GroupMembership) {
    groupMemberships.add(groupMembership)
}

/**
 * Removes all instances of the given [groupMembership] from [NewRawContact.groupMemberships].
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
fun NewRawContact.removeGroupMembership(
    groupMembership: GroupMembership, byReference: Boolean = false
) {
    groupMemberships.removeAll(groupMembership, byReference)
}

/**
 * Clears [NewRawContact.groupMemberships].
 */
fun NewRawContact.removeAllGroupMemberships() {
    groupMemberships.clear()
}

/**
 * Adds the given [im] to [NewRawContact.ims].
 */
fun NewRawContact.addIm(im: NewIm) {
    ims.add(im)
}

/**
 * Adds a new IM (configured by [configureIm]) to [NewRawContact.ims].
 */
fun NewRawContact.addIm(configureIm: NewIm.() -> Unit) {
    ims.add(NewIm().apply(configureIm))
}

/**
 * Removes all instances of the given [im] from [NewRawContact.ims].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun NewRawContact.removeIm(im: NewIm, byReference: Boolean = false) {
    ims.removeAll(im, byReference)
}

/**
 * Clears [NewRawContact.ims].
 */
fun NewRawContact.removeAllIms() {
    ims.clear()
}

/**
 * Sets the [NewRawContact.name] to the given [name].
 */
fun NewRawContact.setName(name: NewName?) {
    this.name = name
}

/**
 * Sets the [NewRawContact.name] (configured by [configureName]) to a new name.
 */
fun NewRawContact.setName(configureName: NewName.() -> Unit) {
    this.name = NewName().apply(configureName)
}

/**
 * Sets the [NewRawContact.nickname] to the given [nickname].
 */
fun NewRawContact.setNickname(nickname: NewNickname?) {
    this.nickname = nickname
}

/**
 * Sets the [NewRawContact.nickname] (configured by [configureNickname]) to a new nickname.
 */
fun NewRawContact.setNickname(configureNickname: NewNickname.() -> Unit) {
    this.nickname = NewNickname().apply(configureNickname)
}

/**
 * Sets the [NewRawContact.note] to the given [note].
 */
fun NewRawContact.setNote(note: NewNote?) {
    this.note = note
}

/**
 * Sets the [NewRawContact.note] (configured by [configureNote]) to a new note.
 */
fun NewRawContact.setNote(configureNote: NewNote.() -> Unit) {
    this.note = NewNote().apply(configureNote)
}

// Options intentionally left out because a Contact and associated RawContacts have independent
// Options.

/**
 * Sets the [NewRawContact.organization] to the given [organization].
 */
fun NewRawContact.setOrganization(organization: NewOrganization?) {
    this.organization = organization
}

/**
 * Sets the [NewRawContact.organization] (configured by [configureOrganization]) to a new org.
 */
fun NewRawContact.setOrganization(configureOrganization: NewOrganization.() -> Unit) {
    this.organization = NewOrganization().apply(configureOrganization)
}

/**
 * Adds the given [phone] to [NewRawContact.phones].
 */
fun NewRawContact.addPhone(phone: NewPhone) {
    phones.add(phone)
}

/**
 * Adds a new phone (configured by [configurePhone]) to [NewRawContact.phones].
 */
fun NewRawContact.addPhone(configurePhone: NewPhone.() -> Unit) {
    phones.add(NewPhone().apply(configurePhone))
}

/**
 * Removes all instances of the given [phone] from [NewRawContact.phones].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun NewRawContact.removePhone(phone: NewPhone, byReference: Boolean = false) {
    phones.removeAll(phone, byReference)
}

/**
 * Clears [NewRawContact.phones].
 */
fun NewRawContact.removeAllPhones() {
    phones.clear()
}

// Photo intentionally left out because a Contact and associated RawContacts have independent
// Photos.

/**
 * Adds the given [relation] to [NewRawContact.relations].
 */
fun NewRawContact.addRelation(relation: NewRelation) {
    relations.add(relation)
}

/**
 * Adds a new relation (configured by [configureRelation]) to [NewRawContact.relations].
 */
fun NewRawContact.addRelation(configureRelation: NewRelation.() -> Unit) {
    relations.add(NewRelation().apply(configureRelation))
}

/**
 * Removes all instances of the given [relation] from [NewRawContact.relations].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun NewRawContact.removeRelation(
    relation: NewRelation,
    byReference: Boolean = false
) {
    relations.removeAll(relation, byReference)
}

/**
 * Clears [NewRawContact.relations].
 */
fun NewRawContact.removeAllRelations() {
    relations.clear()
}

/**
 * Sets the [NewRawContact.sipAddress] to the given [sipAddress].
 */
fun NewRawContact.setSipAddress(sipAddress: NewSipAddress?) {
    this.sipAddress = sipAddress
}

/**
 * Sets the [NewRawContact.sipAddress] (configured by [configureSipAddress]) to a new address.
 */
fun NewRawContact.setSipAddress(configureSipAddress: NewSipAddress.() -> Unit) {
    this.sipAddress = NewSipAddress().apply(configureSipAddress)
}

/**
 * Adds the given [website] to [NewRawContact.websites].
 */
fun NewRawContact.addWebsite(website: NewWebsite) {
    websites.add(website)
}

/**
 * Adds a new website (configured by [configureWebsite]) to [NewRawContact.websites].
 */
fun NewRawContact.addWebsite(configureWebsite: NewWebsite.() -> Unit) {
    websites.add(NewWebsite().apply(configureWebsite))
}

/**
 * Removes all instances of the given [website] from [NewRawContact.websites].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun NewRawContact.removeWebsite(website: NewWebsite, byReference: Boolean = false) {
    websites.removeAll(website, byReference)
}

/**
 * Clears [NewRawContact.websites].
 */
fun NewRawContact.removeAllWebsites() {
    websites.clear()
}