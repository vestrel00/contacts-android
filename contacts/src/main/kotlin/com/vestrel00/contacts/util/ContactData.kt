package com.vestrel00.contacts.util

import com.vestrel00.contacts.entities.*

// Contact

fun Contact.addresses(): Sequence<Address> = rawContacts
    .asSequence()
    .flatMap { it.addresses.asSequence() }
    .sortedBy { it.formattedAddress }

fun Contact.companies(): Sequence<Company> = rawContacts
    .asSequence()
    .filter { it.company != null }
    .map { it.company!! }
    .sortedBy { it.company }

fun Contact.emails(): Sequence<Email> = rawContacts
    .asSequence()
    .flatMap { it.emails.asSequence() }
    .sortedBy { it.address }

fun Contact.events(): Sequence<Event> = rawContacts
    .asSequence()
    .flatMap { it.events.asSequence() }
    .sortedBy { it.date }

// GroupMemberships are intentionally left out because they should never be combined.
// The native Contacts app hides the group membership UI field when viewing/editing a contact
// with more than one RawContact.

fun Contact.ims(): Sequence<Im> = rawContacts
    .asSequence()
    .flatMap { it.ims.asSequence() }
    .sortedBy { it.data }

fun Contact.names(): Sequence<Name> = rawContacts
    .asSequence()
    .filter { it.name != null }
    .map { it.name!! }
    .sortedBy { it.displayName }

fun Contact.nicknames(): Sequence<Nickname> = rawContacts
    .asSequence()
    .filter { it.nickname != null }
    .map { it.nickname!! }
    .sortedBy { it.name }

fun Contact.notes(): Sequence<Note> = rawContacts
    .asSequence()
    .filter { it.note != null }
    .map { it.note!! }
    .sortedBy { it.note }

// Options intentionally left out because a Contact and associated RawContacts have independent
// Options.

fun Contact.phones(): Sequence<Phone> = rawContacts
    .asSequence()
    .flatMap { it.phones.asSequence() }
    .sortedBy { it.number }

fun Contact.relations(): Sequence<Relation> = rawContacts
    .asSequence()
    .flatMap { it.relations.asSequence() }
    .sortedBy { it.name }

fun Contact.sipAddresses(): Sequence<SipAddress> = rawContacts
    .asSequence()
    .filter { it.sipAddress != null }
    .map { it.sipAddress!! }
    .sortedBy { it.sipAddress }

fun Contact.websites(): Sequence<Website> = rawContacts
    .asSequence()
    .flatMap { it.websites.asSequence() }
    .sortedBy { it.url }

// MutableContact

fun MutableContact.addresses(): Sequence<MutableAddress> = rawContacts
    .asSequence()
    .flatMap { it.addresses.asSequence() }
    .sortedBy { it.formattedAddress }

fun MutableContact.addAddress(address: MutableAddress) {
    rawContacts.firstOrNull()?.addresses?.add(address)
}

fun MutableContact.removeAddress(address: MutableAddress, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.addresses.removeAll(address, byReference)
    }
}

fun MutableContact.companies(): Sequence<MutableCompany> = rawContacts
    .asSequence()
    .filter { it.company != null }
    .map { it.company!! }
    .sortedBy { it.company }

fun MutableContact.setCompany(company: MutableCompany) {
    rawContacts.firstOrNull()?.company = company
}

fun MutableContact.emails(): Sequence<MutableEmail> = rawContacts
    .asSequence()
    .flatMap { it.emails.asSequence() }
    .sortedBy { it.address }

fun MutableContact.addEmail(email: MutableEmail) {
    rawContacts.firstOrNull()?.emails?.add(email)
}

fun MutableContact.removeEmail(email: MutableEmail, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.emails.removeAll(email, byReference)
    }
}

fun MutableContact.events(): Sequence<MutableEvent> = rawContacts
    .asSequence()
    .flatMap { it.events.asSequence() }
    .sortedBy { it.date }

fun MutableContact.addEvent(event: MutableEvent) {
    rawContacts.firstOrNull()?.events?.add(event)
}

fun MutableContact.removeEvent(event: MutableEvent, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.events.removeAll(event, byReference)
    }
}

// GroupMemberships are intentionally left out because they should never be combined.
// The native Contacts app hides the group membership UI field when viewing/editing a contact
// with more than one RawContact.

fun MutableContact.ims(): Sequence<MutableIm> = rawContacts
    .asSequence()
    .flatMap { it.ims.asSequence() }
    .sortedBy { it.data }

fun MutableContact.addIm(im: MutableIm) {
    rawContacts.firstOrNull()?.ims?.add(im)
}

fun MutableContact.removeIm(im: MutableIm, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.ims.removeAll(im, byReference)
    }
}

fun MutableContact.names(): Sequence<MutableName> = rawContacts
    .asSequence()
    .filter { it.name != null }
    .map { it.name!! }
    .sortedBy { it.displayName }

fun MutableContact.setName(name: MutableName) {
    rawContacts.firstOrNull()?.name = name
}

fun MutableContact.nicknames(): Sequence<MutableNickname> = rawContacts
    .asSequence()
    .filter { it.nickname != null }
    .map { it.nickname!! }
    .sortedBy { it.name }

fun MutableContact.setNickname(nickname: MutableNickname) {
    rawContacts.firstOrNull()?.nickname = nickname
}

fun MutableContact.notes(): Sequence<MutableNote> = rawContacts
    .asSequence()
    .filter { it.note != null }
    .map { it.note!! }
    .sortedBy { it.note }

fun MutableContact.setNote(note: MutableNote) {
    rawContacts.firstOrNull()?.note = note
}

// Options intentionally left out because a Contact and associated RawContacts have independent
// Options.

fun MutableContact.phones(): Sequence<MutablePhone> = rawContacts
    .asSequence()
    .flatMap { it.phones.asSequence() }
    .sortedBy { it.number }

fun MutableContact.addPhone(phone: MutablePhone) {
    rawContacts.firstOrNull()?.phones?.add(phone)
}

fun MutableContact.removePhone(phone: MutablePhone, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.phones.removeAll(phone, byReference)
    }
}

fun MutableContact.relations(): Sequence<MutableRelation> = rawContacts
    .asSequence()
    .flatMap { it.relations.asSequence() }
    .sortedBy { it.name }

fun MutableContact.addRelation(relation: MutableRelation) {
    rawContacts.firstOrNull()?.relations?.add(relation)
}


fun MutableContact.removeRelation(relation: MutableRelation, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.relations.removeAll(relation, byReference)
    }
}

fun MutableContact.sipAddresses(): Sequence<MutableSipAddress> = rawContacts
    .asSequence()
    .filter { it.sipAddress != null }
    .map { it.sipAddress!! }
    .sortedBy { it.sipAddress }

fun MutableContact.setSipAddress(sipAddress: MutableSipAddress) {
    rawContacts.firstOrNull()?.sipAddress = sipAddress
}

fun MutableContact.websites(): Sequence<MutableWebsite> = rawContacts
    .asSequence()
    .flatMap { it.websites.asSequence() }
    .sortedBy { it.url }

fun MutableContact.addWebsite(website: MutableWebsite) {
    rawContacts.firstOrNull()?.websites?.add(website)
}

fun MutableContact.removeWebsite(website: MutableWebsite, byReference: Boolean = false) {
    for (rawContact in rawContacts) {
        rawContact.websites.removeAll(website, byReference)
    }
}

fun <T> MutableCollection<T>.removeAll(instance: T, byReference: Boolean = false) {
    if (byReference) {
        removeAll { it === instance }
    } else {
        removeAll { it == instance }
    }
}