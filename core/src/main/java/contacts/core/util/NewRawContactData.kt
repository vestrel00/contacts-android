package contacts.core.util

import android.accounts.Account
import contacts.core.entities.NewAddress
import contacts.core.entities.NewCustomDataEntity
import contacts.core.entities.NewDataEntity
import contacts.core.entities.NewEmail
import contacts.core.entities.NewEvent
import contacts.core.entities.NewGroupMembership
import contacts.core.entities.NewIm
import contacts.core.entities.NewName
import contacts.core.entities.NewNickname
import contacts.core.entities.NewNote
import contacts.core.entities.NewOptions
import contacts.core.entities.NewOrganization
import contacts.core.entities.NewPhone
import contacts.core.entities.NewRawContact
import contacts.core.entities.NewRelation
import contacts.core.entities.NewSipAddress
import contacts.core.entities.NewWebsite
import contacts.core.entities.removeAll
import contacts.core.redactedCopyOrThis

/**
 * Sets the given [NewRawContact.account] to the given [account].
 */
fun NewRawContact.setAccount(account: Account?) {
    this.account = account?.redactedCopyOrThis(isRedacted)
}

/**
 * Adds the given [address] to [NewRawContact.addresses].
 */
fun NewRawContact.addAddress(address: NewAddress) {
    addresses.add(address.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new address (configured by [configureAddress]) to [NewRawContact.addresses].
 */
fun NewRawContact.addAddress(configureAddress: NewAddress.() -> Unit) {
    addAddress(NewAddress().apply(configureAddress))
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
    emails.add(email.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new email (configured by [configureEmail]) to [NewRawContact.emails].
 */
fun NewRawContact.addEmail(configureEmail: NewEmail.() -> Unit) {
    addEmail(NewEmail().apply(configureEmail))
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
    events.add(event.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new event (configured by [configureEvent]) to [NewRawContact.events].
 */
fun NewRawContact.addEvent(configureEvent: NewEvent.() -> Unit) {
    addEvent(NewEvent().apply(configureEvent))
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
 */
fun NewRawContact.addGroupMembership(groupMembership: NewGroupMembership) {
    groupMemberships.add(groupMembership.redactedCopyOrThis(isRedacted))
}

/**
 * Removes all instances of the given [groupMembership] from [NewRawContact.groupMemberships].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun NewRawContact.removeGroupMembership(
    groupMembership: NewGroupMembership, byReference: Boolean = false
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
    ims.add(im.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new IM (configured by [configureIm]) to [NewRawContact.ims].
 */
fun NewRawContact.addIm(configureIm: NewIm.() -> Unit) {
    addIm(NewIm().apply(configureIm))
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
    this.name = name?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [NewRawContact.name] (configured by [configureName]) to a new name.
 */
fun NewRawContact.setName(configureName: NewName.() -> Unit) {
    setName(NewName().apply(configureName))
}

/**
 * Sets the [NewRawContact.nickname] to the given [nickname].
 */
fun NewRawContact.setNickname(nickname: NewNickname?) {
    this.nickname = nickname?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [NewRawContact.nickname] (configured by [configureNickname]) to a new nickname.
 */
fun NewRawContact.setNickname(configureNickname: NewNickname.() -> Unit) {
    setNickname(NewNickname().apply(configureNickname))
}

/**
 * Sets the [NewRawContact.note] to the given [note].
 */
fun NewRawContact.setNote(note: NewNote?) {
    this.note = note?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [NewRawContact.note] (configured by [configureNote]) to a new note.
 */
fun NewRawContact.setNote(configureNote: NewNote.() -> Unit) {
    setNote(NewNote().apply(configureNote))
}

/**
 * Sets the [NewRawContact.options] to the given [options].
 */
fun NewRawContact.setOptions(options: NewOptions?) {
    this.options = options?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [NewRawContact.options] (configured by [configureOptions]) to a new options.
 */
fun NewRawContact.setOptions(configureOptions: NewOptions.() -> Unit) {
    setOptions(NewOptions().apply(configureOptions))
}

/**
 * Sets the [NewRawContact.organization] to the given [organization].
 */
fun NewRawContact.setOrganization(organization: NewOrganization?) {
    this.organization = organization?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [NewRawContact.organization] (configured by [configureOrganization]) to a new org.
 */
fun NewRawContact.setOrganization(configureOrganization: NewOrganization.() -> Unit) {
    setOrganization(NewOrganization().apply(configureOrganization))
}

/**
 * Adds the given [phone] to [NewRawContact.phones].
 */
fun NewRawContact.addPhone(phone: NewPhone) {
    phones.add(phone.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new phone (configured by [configurePhone]) to [NewRawContact.phones].
 */
fun NewRawContact.addPhone(configurePhone: NewPhone.() -> Unit) {
    addPhone(NewPhone().apply(configurePhone))
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

/**
 * Adds the given [relation] to [NewRawContact.relations].
 */
fun NewRawContact.addRelation(relation: NewRelation) {
    relations.add(relation.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new relation (configured by [configureRelation]) to [NewRawContact.relations].
 */
fun NewRawContact.addRelation(configureRelation: NewRelation.() -> Unit) {
    addRelation(NewRelation().apply(configureRelation))
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
    this.sipAddress = sipAddress?.redactedCopyOrThis(isRedacted)
}

/**
 * Sets the [NewRawContact.sipAddress] (configured by [configureSipAddress]) to a new address.
 */
fun NewRawContact.setSipAddress(configureSipAddress: NewSipAddress.() -> Unit) {
    setSipAddress(NewSipAddress().apply(configureSipAddress))
}

/**
 * Adds the given [website] to [NewRawContact.websites].
 */
fun NewRawContact.addWebsite(website: NewWebsite) {
    websites.add(website.redactedCopyOrThis(isRedacted))
}

/**
 * Adds a new website (configured by [configureWebsite]) to [NewRawContact.websites].
 */
fun NewRawContact.addWebsite(configureWebsite: NewWebsite.() -> Unit) {
    addWebsite((NewWebsite().apply(configureWebsite)))
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

/**
 * Sequence of all data kinds (e.g. addresses, emails, events, etc) of this [NewRawContact].
 */
fun NewRawContact.data(): Sequence<NewDataEntity> = sequence {
    yieldAll(addresses)
    yieldAll(emails)
    yieldAll(events)
    // Group memberships are implicitly read-only.
    yieldAll(ims)
    name?.also { yield(it) }
    nickname?.also { yield(it) }
    note?.also { yield(it) }
    organization?.also { yield(it) }
    yieldAll(phones)
    // Photo is implicitly read-only.
    yieldAll(relations)
    sipAddress?.also { yield(it) }
    yieldAll(websites)

    yieldAll(
        customDataEntities.values
            .flatMap { it.entities }
            .filterIsInstance<NewCustomDataEntity>()
    )
}

/**
 * Same as [NewRawContact.data] but as a [List].
 */
fun NewRawContact.dataList(): List<NewDataEntity> = data().toList()

/**
 * Sets the value of all [NewDataEntity.isReadOnly] (including any custom data) to [readOnly].
 *
 * This is useful if you are passing a [NewRawContact] into an insert API and you want all of its
 * data to be read-only or not.
 *
 * ## A few things to note!
 *
 * 1. This does not set the RawContact itself to be read-only. This only sets the read-only flag of
 * all the data belonging to this RawContact.
 * 2. Only data that have already been set/added into this instance will have their read-only
 * properties set. Other data set/added after the call to this function will not have their
 * read-only properties set. For example; [addEmail] -> [setDataAsReadOnly] -> [addPhone]. Only the
 * added email has its read-only property set. The phone did not. Therefore, if you want all data to
 * have the same read-only value, then you should invoke this function after all data has been
 * added/set.
 */
fun NewRawContact.setDataAsReadOnly(readOnly: Boolean) {
    data().forEach { it.isReadOnly = readOnly }
}