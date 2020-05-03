package com.vestrel00.contacts.util

import com.vestrel00.contacts.entities.*

// region Contact

/**
 * Sequence of addresses from all [rawContacts] ordered by the [Address.formattedAddress].
 */
fun Contact.addresses(): Sequence<Address> = rawContacts
    .asSequence()
    .flatMap { it.addresses.asSequence() }
    .sortedBy { it.formattedAddress }

/**
 * Sequence of companies from all [rawContacts] ordered by the [Company.company].
 */
fun Contact.companies(): Sequence<Company> = rawContacts
    .asSequence()
    .map { it.company }
    .filterNotNull()
    .sortedBy { it.company }

/**
 * Sequence of emails from all [rawContacts] ordered by the [Email.address].
 */
fun Contact.emails(): Sequence<Email> = rawContacts
    .asSequence()
    .flatMap { it.emails.asSequence() }
    .sortedBy { it.address }

/**
 * Sequence of events from all [rawContacts] ordered by the [Event.date].
 */
fun Contact.events(): Sequence<Event> = rawContacts
    .asSequence()
    .flatMap { it.events.asSequence() }
    .sortedBy { it.date }

// GroupMemberships are intentionally left out because they should never be combined.
// The native Contacts app hides the group membership UI field when viewing/editing a contact
// with more than one RawContact.

/**
 * Sequence of Ims from all [rawContacts] ordered by the [Im.data].
 */
fun Contact.ims(): Sequence<Im> = rawContacts
    .asSequence()
    .flatMap { it.ims.asSequence() }
    .sortedBy { it.data }

/**
 * Sequence of names from all [rawContacts] ordered by the [Name.displayName].
 */
fun Contact.names(): Sequence<Name> = rawContacts
    .asSequence()
    .map { it.name }
    .filterNotNull()
    .sortedBy { it.displayName }

/**
 * Sequence of nicknames from all [rawContacts] ordered by the [Nickname.name].
 */
fun Contact.nicknames(): Sequence<Nickname> = rawContacts
    .asSequence()
    .map { it.nickname }
    .filterNotNull()
    .sortedBy { it.name }

/**
 * Sequence of notes from all [rawContacts] ordered by the [Note.note].
 */
fun Contact.notes(): Sequence<Note> = rawContacts
    .asSequence()
    .map { it.note }
    .filterNotNull()
    .sortedBy { it.note }

// Options intentionally left out because a Contact and associated RawContacts have independent
// Options.

/**
 * Sequence of phones from all [rawContacts] ordered by the [Phone.number].
 */
fun Contact.phones(): Sequence<Phone> = rawContacts
    .asSequence()
    .flatMap { it.phones.asSequence() }
    .sortedBy { it.number }

// Photo intentionally left out because a Contact and associated RawContacts have independent
// Photos.

/**
 * Sequence of relations from all [rawContacts] ordered by the [Relation.name].
 */
fun Contact.relations(): Sequence<Relation> = rawContacts
    .asSequence()
    .flatMap { it.relations.asSequence() }
    .sortedBy { it.name }

/**
 * Sequence of SIP addresses from all [rawContacts] ordered by the [SipAddress.sipAddress].
 */
fun Contact.sipAddresses(): Sequence<SipAddress> = rawContacts
    .asSequence()
    .map { it.sipAddress }
    .filterNotNull()
    .sortedBy { it.sipAddress }

/**
 * Sequence of websites from all [rawContacts] ordered by the [Website.url].
 */
fun Contact.websites(): Sequence<Website> = rawContacts
    .asSequence()
    .flatMap { it.websites.asSequence() }
    .sortedBy { it.url }

// endregion

// region MutableContact

/**
 * Sequence of addresses from all [rawContacts] ordered by the
 * [MutableAddress.formattedAddress].
 */
fun MutableContact.addresses(): Sequence<MutableAddress> = rawContacts
    .asSequence()
    .flatMap { it.addresses.asSequence() }
    .sortedBy { it.formattedAddress }

/**
 * Adds the given [address] to the list of [RawContact.addresses] of the first [RawContact] in
 * [MutableContact.rawContacts] sorted by the [RawContact.id] sorted by the [RawContact.id].
 */
fun MutableContact.addAddress(address: MutableAddress) {
    rawContacts.firstOrNull()?.addresses?.add(address)
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
        rawContact.addresses.removeAll(address, byReference)
    }
}

/**
 * Removes all [addresses] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllAddresses() {
    for (rawContact in rawContacts) {
        rawContact.addresses.clear()
    }
}

/**
 * Sequence of companies from all [rawContacts] ordered by the [MutableCompany.company].
 */
fun MutableContact.companies(): Sequence<MutableCompany> = rawContacts
    .asSequence()
    .map { it.company }
    .filterNotNull()
    .sortedBy { it.company }

/**
 * Sets the [RawContact.company] of the first [RawContact] in [MutableContact.rawContacts] sorted by
 * the [RawContact.id].
 */
fun MutableContact.setCompany(company: MutableCompany?) {
    rawContacts.firstOrNull()?.company = company
}

/**
 * Sequence of emails from all [rawContacts] ordered by the [MutableEmail.address].
 */
fun MutableContact.emails(): Sequence<MutableEmail> = rawContacts
    .asSequence()
    .flatMap { it.emails.asSequence() }
    .sortedBy { it.address }

/**
 * Adds the given [email] to the list of [RawContact.emails] of the first [RawContact] in
 * [MutableContact.rawContacts] sorted by the [RawContact.id].
 */
fun MutableContact.addEmail(email: MutableEmail) {
    rawContacts.firstOrNull()?.emails?.add(email)
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
        rawContact.emails.removeAll(email, byReference)
    }
}

/**
 * Removes all [emails] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllEmails() {
    for (rawContact in rawContacts) {
        rawContact.emails.clear()
    }
}

/**
 * Sequence of events from all [rawContacts] ordered by the [MutableEvent.date].
 */
fun MutableContact.events(): Sequence<MutableEvent> = rawContacts
    .asSequence()
    .flatMap { it.events.asSequence() }
    .sortedBy { it.date }

/**
 * Adds the given [event] to the list of [RawContact.events] of the first [RawContact] in
 * [MutableContact.rawContacts] sorted by the [RawContact.id].
 */
fun MutableContact.addEvent(event: MutableEvent) {
    rawContacts.firstOrNull()?.events?.add(event)
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
        rawContact.events.removeAll(event, byReference)
    }
}

/**
 * Removes all [events] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllEvents() {
    for (rawContact in rawContacts) {
        rawContact.events.clear()
    }
}

// GroupMemberships are intentionally left out because they should never be combined.
// The native Contacts app hides the group membership UI field when viewing/editing a contact
// with more than one RawContact.

/**
 * Sequence of Ims from all [rawContacts] ordered by the [MutableIm.data].
 */
fun MutableContact.ims(): Sequence<MutableIm> = rawContacts
    .asSequence()
    .flatMap { it.ims.asSequence() }
    .sortedBy { it.data }

/**
 * Adds the given [im] to the list of [RawContact.ims] of the first [RawContact] in
 * [MutableContact.rawContacts] sorted by the [RawContact.id].
 */
fun MutableContact.addIm(im: MutableIm) {
    rawContacts.firstOrNull()?.ims?.add(im)
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
        rawContact.ims.removeAll(im, byReference)
    }
}

/**
 * Removes all [ims] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllIms() {
    for (rawContact in rawContacts) {
        rawContact.ims.clear()
    }
}

/**
 * Sequence of names from all [rawContacts] ordered by the [MutableName.displayName].
 */
fun MutableContact.names(): Sequence<MutableName> = rawContacts
    .asSequence()
    .map { it.name }
    .filterNotNull()
    .sortedBy { it.displayName }

/**
 * Sets the [RawContact.name] of the first [RawContact] in [MutableContact.rawContacts] sorted by
 * the [RawContact.id].
 */
fun MutableContact.setName(name: MutableName?) {
    rawContacts.firstOrNull()?.name = name
}

/**
 * Sequence of nicknames from all [rawContacts] ordered by the [MutableNickname.name].
 */
fun MutableContact.nicknames(): Sequence<MutableNickname> = rawContacts
    .asSequence()
    .map { it.nickname }
    .filterNotNull()
    .sortedBy { it.name }

/**
 * Sets the [RawContact.nickname] of the first [RawContact] in [MutableContact.rawContacts] sorted
 * by the [RawContact.id].
 */
fun MutableContact.setNickname(nickname: MutableNickname?) {
    rawContacts.firstOrNull()?.nickname = nickname
}

/**
 * Sequence of notes from all [rawContacts] ordered by the [MutableNote.note].
 */
fun MutableContact.notes(): Sequence<MutableNote> = rawContacts
    .asSequence()
    .map { it.note }
    .filterNotNull()
    .sortedBy { it.note }

/**
 * Sets the [RawContact.note] of the first [RawContact] in [MutableContact.rawContacts] sorted by
 * the [RawContact.id].
 */
fun MutableContact.setNote(note: MutableNote?) {
    rawContacts.firstOrNull()?.note = note
}

// Options intentionally left out because a Contact and associated RawContacts have independent
// Options.

/**
 * Sequence of phones from all [rawContacts] ordered by the [MutablePhone.number].
 */
fun MutableContact.phones(): Sequence<MutablePhone> = rawContacts
    .asSequence()
    .flatMap { it.phones.asSequence() }
    .sortedBy { it.number }

/**
 * Adds the given [phone] to the list of [RawContact.phones] of the first [RawContact] in
 * [MutableContact.rawContacts] sorted by the [RawContact.id].
 */
fun MutableContact.addPhone(phone: MutablePhone) {
    rawContacts.firstOrNull()?.phones?.add(phone)
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
        rawContact.phones.removeAll(phone, byReference)
    }
}

/**
 * Removes all [phones] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllPhones() {
    for (rawContact in rawContacts) {
        rawContact.phones.clear()
    }
}

// Photo intentionally left out because a Contact and associated RawContacts have independent
// Photos.

/**
 * Sequence of relations from all [rawContacts] ordered by the [MutableRelation.name].
 */
fun MutableContact.relations(): Sequence<MutableRelation> = rawContacts
    .asSequence()
    .flatMap { it.relations.asSequence() }
    .sortedBy { it.name }

/**
 * Adds the given [relation] to the list of [RawContact.relations] of the first [RawContact] in
 * [MutableContact.rawContacts] sorted by the [RawContact.id].
 */
fun MutableContact.addRelation(relation: MutableRelation) {
    rawContacts.firstOrNull()?.relations?.add(relation)
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
        rawContact.relations.removeAll(relation, byReference)
    }
}

/**
 * Removes all [relations] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllRelations() {
    for (rawContact in rawContacts) {
        rawContact.relations.clear()
    }
}

/**
 * Sequence of SIP addresses from all [rawContacts] ordered by the
 * [MutableSipAddress.sipAddress].
 */
fun MutableContact.sipAddresses(): Sequence<MutableSipAddress> = rawContacts
    .asSequence()
    .map { it.sipAddress }
    .filterNotNull()
    .sortedBy { it.sipAddress }

/**
 * Sets the [RawContact.sipAddress] of the first [RawContact] in [MutableContact.rawContacts] sorted
 * by the [RawContact.id].
 */
fun MutableContact.setSipAddress(sipAddress: MutableSipAddress?) {
    rawContacts.firstOrNull()?.sipAddress = sipAddress
}

/**
 * Sequence of websites from all [rawContacts] ordered by the [MutableWebsite.url].
 */
fun MutableContact.websites(): Sequence<MutableWebsite> = rawContacts
    .asSequence()
    .flatMap { it.websites.asSequence() }
    .sortedBy { it.url }

/**
 * Adds the given [website] to the list of [RawContact.websites] of the first [RawContact] in
 * [MutableContact.rawContacts] sorted by the [RawContact.id].
 */
fun MutableContact.addWebsite(website: MutableWebsite) {
    rawContacts.firstOrNull()?.websites?.add(website)
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
        rawContact.websites.removeAll(website, byReference)
    }
}

/**
 * Removes all [websites] from all of the [MutableContact.rawContacts].
 */
fun MutableContact.removeAllWebsites() {
    for (rawContact in rawContacts) {
        rawContact.websites.clear()
    }
}

// endregion

/**
 * Removes all instances of the given [instance] from [this] collectiomn.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun <T : Entity> MutableCollection<T>.removeAll(instance: T, byReference: Boolean = false) {
    if (byReference) {
        removeAll { it === instance }
    } else {
        removeAll { it == instance }
    }
}