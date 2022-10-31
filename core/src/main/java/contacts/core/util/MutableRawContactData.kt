package contacts.core.util

import contacts.core.entities.*
import contacts.core.redactedCopyOrThis

/**
 * Adds the given [address] to [MutableRawContact.addresses].
 */
fun MutableRawContact.addAddress(address: MutableAddressEntity) {
    addresses.add(address.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new address (configured by [configureAddress]) to [MutableRawContact.addresses].
 */
fun MutableRawContact.addAddress(configureAddress: NewAddress.() -> Unit) {
    addAddress(NewAddress().apply(configureAddress))
}

/**
 * Removes all instances of the given [address] from [MutableRawContact.addresses].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeAddress(address: MutableAddressEntity, byReference: Boolean = false) {
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
fun MutableRawContact.addEmail(email: MutableEmailEntity) {
    emails.add(email.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new email (configured by [configureEmail]) to [MutableRawContact.emails].
 */
fun MutableRawContact.addEmail(configureEmail: NewEmail.() -> Unit) {
    addEmail(NewEmail().apply(configureEmail))
}

/**
 * Removes all instances of the given [email] from [MutableRawContact.emails].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeEmail(email: MutableEmailEntity, byReference: Boolean = false) {
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
fun MutableRawContact.addEvent(event: MutableEventEntity) {
    events.add(event.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new event (configured by [configureEvent]) to [MutableRawContact.events].
 */
fun MutableRawContact.addEvent(configureEvent: NewEvent.() -> Unit) {
    addEvent(NewEvent().apply(configureEvent))
}

/**
 * Removes all instances of the given [event] from [MutableRawContact.events]
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeEvent(event: MutableEventEntity, byReference: Boolean = false) {
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
fun MutableRawContact.addGroupMembership(groupMembership: GroupMembershipEntity) {
    groupMemberships.add(groupMembership.redactedCopyOrThis(isRedacted))
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
    groupMembership: GroupMembershipEntity, byReference: Boolean = false
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
fun MutableRawContact.addIm(im: MutableImEntity) {
    ims.add(im.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new IM (configured by [configureIm]) to [MutableRawContact.ims].
 */
fun MutableRawContact.addIm(configureIm: NewIm.() -> Unit) {
    addIm(NewIm().apply(configureIm))
}

/**
 * Removes all instances of the given [im] from [MutableRawContact.ims].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeIm(im: MutableImEntity, byReference: Boolean = false) {
    ims.removeAll(im, byReference)
}

/**
 * Clears [MutableRawContact.ims].
 */
fun MutableRawContact.removeAllIms() {
    ims.clear()
}

/**
 * Sets the [MutableRawContact.name] to the given [name].
 */
fun MutableRawContact.setName(name: MutableNameEntity?) {
    this.name = name?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [MutableRawContact.name] (configured by [configureName]) to a new name.
 */
fun MutableRawContact.setName(configureName: NewName.() -> Unit) {
    setName(NewName().apply(configureName))
}

/**
 * Sets the [MutableRawContact.nickname] to the given [nickname].
 */
fun MutableRawContact.setNickname(nickname: MutableNicknameEntity?) {
    this.nickname = nickname?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [MutableRawContact.nickname] (configured by [configureNickname]) to a new nickname.
 */
fun MutableRawContact.setNickname(configureNickname: NewNickname.() -> Unit) {
    setNickname(NewNickname().apply(configureNickname))
}

/**
 * Sets the [MutableRawContact.note] to the given [note].
 */
fun MutableRawContact.setNote(note: MutableNoteEntity?) {
    this.note = note?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [MutableRawContact.note] (configured by [configureNote]) to a new note.
 */
fun MutableRawContact.setNote(configureNote: NewNote.() -> Unit) {
    setNote(NewNote().apply(configureNote))
}

/**
 * Sets the [MutableRawContact.options] to the given [options].
 */
fun MutableRawContact.setOptions(options: MutableOptionsEntity?) {
    this.options = options?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [MutableRawContact.options] (configured by [configureOptions]) to a new options.
 */
fun MutableRawContact.setOptions(configureOptions: NewOptions.() -> Unit) {
    setOptions(NewOptions().apply(configureOptions))
}

/**
 * Sets the [MutableRawContact.organization] to the given [organization].
 */
fun MutableRawContact.setOrganization(organization: MutableOrganizationEntity?) {
    this.organization = organization?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [MutableRawContact.organization] (configured by [configureOrganization]) to a new org.
 */
fun MutableRawContact.setOrganization(configureOrganization: NewOrganization.() -> Unit) {
    setOrganization(NewOrganization().apply(configureOrganization))
}

/**
 * Adds the given [phone] to [MutableRawContact.phones].
 */
fun MutableRawContact.addPhone(phone: MutablePhoneEntity) {
    phones.add(phone.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new phone (configured by [configurePhone]) to [MutableRawContact.phones].
 */
fun MutableRawContact.addPhone(configurePhone: NewPhone.() -> Unit) {
    addPhone(NewPhone().apply(configurePhone))
}

/**
 * Removes all instances of the given [phone] from [MutableRawContact.phones].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removePhone(phone: MutablePhoneEntity, byReference: Boolean = false) {
    phones.removeAll(phone, byReference)
}

/**
 * Clears [MutableRawContact.phones].
 */
fun MutableRawContact.removeAllPhones() {
    phones.clear()
}

/**
 * Adds the given [relation] to [MutableRawContact.relations].
 */
fun MutableRawContact.addRelation(relation: MutableRelationEntity) {
    relations.add(relation.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new relation (configured by [configureRelation]) to [MutableRawContact.relations].
 */
fun MutableRawContact.addRelation(configureRelation: NewRelation.() -> Unit) {
    addRelation(NewRelation().apply(configureRelation))
}

/**
 * Removes all instances of the given [relation] from [MutableRawContact.relations].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeRelation(
    relation: MutableRelationEntity,
    byReference: Boolean = false
) {
    relations.removeAll(relation, byReference)
}

/**
 * Clears [MutableRawContact.relations].
 */
fun MutableRawContact.removeAllRelations() {
    relations.clear()
}

/**
 * Sets the [MutableRawContact.sipAddress] to the given [sipAddress].
 */
fun MutableRawContact.setSipAddress(sipAddress: MutableSipAddressEntity?) {
    this.sipAddress = sipAddress?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [MutableRawContact.sipAddress] (configured by [configureSipAddress]) to a new address.
 */
fun MutableRawContact.setSipAddress(configureSipAddress: NewSipAddress.() -> Unit) {
    setSipAddress(NewSipAddress().apply(configureSipAddress))
}

/**
 * Adds the given [website] to [MutableRawContact.websites].
 */
fun MutableRawContact.addWebsite(website: MutableWebsiteEntity) {
    websites.add(website.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new website (configured by [configureWebsite]) to [MutableRawContact.websites].
 */
fun MutableRawContact.addWebsite(configureWebsite: NewWebsite.() -> Unit) {
    addWebsite(NewWebsite().apply(configureWebsite))
}

/**
 * Removes all instances of the given [website] from [MutableRawContact.websites].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun MutableRawContact.removeWebsite(website: MutableWebsiteEntity, byReference: Boolean = false) {
    websites.removeAll(website, byReference)
}

/**
 * Clears [MutableRawContact.websites].
 */
fun MutableRawContact.removeAllWebsites() {
    websites.clear()
}