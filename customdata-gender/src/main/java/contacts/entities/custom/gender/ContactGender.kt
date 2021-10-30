package contacts.entities.custom.gender

import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.MutableContact
import contacts.core.entities.RawContact

/**
 * Returns the sequence of [Gender]s from all [Contact.rawContacts] ordered by the [Gender.id].
 */
fun Contact.genders(contacts: Contacts): Sequence<Gender> = rawContacts
    .asSequence()
    .mapNotNull { it.gender(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [Gender]s from all [Contact.rawContacts] ordered by the [Gender.id].
 */
fun Contact.genderList(contacts: Contacts): List<Gender> = genders(contacts).toList()

/**
 * Returns the sequence of [MutableGender]s from all [Contact.rawContacts] ordered by the
 * [MutableGender.id].
 */
fun MutableContact.genders(contacts: Contacts): Sequence<MutableGender> = rawContacts
    .asSequence()
    .mapNotNull { it.gender(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [MutableGender]s from all [Contact.rawContacts] ordered by the
 * [MutableGender.id].
 */
fun MutableContact.genderList(contacts: Contacts): List<MutableGender> = genders(contacts).toList()

/**
 * Sets the [RawContact.gender] of the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableContact] object.
 */
fun MutableContact.setGender(contacts: Contacts, gender: MutableGender?) {
    rawContacts.firstOrNull()?.setGender(contacts, gender)
}

/**
 * Sets the [RawContact.gender] (configured by [configureGender]) of the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableContact] object.
 */
fun MutableContact.setGender(contacts: Contacts, configureGender: MutableGender.() -> Unit) {
    setGender(contacts, MutableGender().apply(configureGender))
}